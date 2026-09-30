package ingsoftware.zeroshop.service.catalog;

import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de precios, historial de variaciones y
 * actualización periódica (bimestral) por contexto de inflación en Argentina.
 */
@Service
public class PriceService {

    public static final int BIMONTHLY_DAYS_THRESHOLD = 60;

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;

    public PriceService(PriceHistoryRepository priceHistoryRepository,
                        ProductRepository productRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Optional<PriceHistory> getCurrentPrice(UUID productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId);
    }

    @Transactional(readOnly = true)
    public List<PriceHistory> getPriceHistory(UUID productId) {
        if (productId == null) {
            return List.of();
        }
        return priceHistoryRepository.findByProductIdAndDeletedFalseOrderByStartDateDesc(productId);
    }

    @Transactional
    public PriceHistory updateProductPrice(UUID productId, BigDecimal newPrice, Boolean onSale) {
        if (productId == null) {
            throw new IllegalArgumentException("El ID de producto no puede ser nulo.");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El nuevo precio debe ser mayor o igual a cero.");
        }

        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        LocalDateTime now = LocalDateTime.now();

        // Cerrar vigencia del precio anterior si existía
        Optional<PriceHistory> currentPriceOpt = priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId);
        currentPriceOpt.ifPresent(prev -> {
            prev.setEndDate(now);
            priceHistoryRepository.save(prev);
        });

        PriceHistory newPriceRecord = PriceHistory.builder()
                .product(product)
                .price(newPrice.setScale(2, RoundingMode.HALF_UP))
                .startDate(now)
                .deleted(false)
                .build();

        PriceHistory saved = priceHistoryRepository.save(newPriceRecord);

        product.setCurrentPrice(saved.getPrice());
        if (onSale != null) {
            product.setOnSale(onSale);
        }
        productRepository.save(product);

        return saved;
    }

    @Transactional(readOnly = true)
    public boolean isPriceUpdateDue(UUID productId) {
        Optional<PriceHistory> current = getCurrentPrice(productId);
        if (current.isEmpty()) {
            return true;
        }
        LocalDateTime startDate = current.get().getStartDate();
        return ChronoUnit.DAYS.between(startDate, LocalDateTime.now()) >= BIMONTHLY_DAYS_THRESHOLD;
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsRequiringPriceUpdate() {
        List<Product> products = productRepository.findAllByDeletedFalse();
        List<Product> dueList = new ArrayList<>();
        for (Product product : products) {
            if (isPriceUpdateDue(product.getId())) {
                dueList.add(product);
            }
        }
        return dueList;
    }

    /**
     * Aplica un ajuste porcentual de precios por inflación (ajuste bimestral requerido por el integrador).
     * @param percentage Porcentaje de incremento (ej: 8.5 para un 8.5%)
     * @param onlyIfDue Si es true, solo se ajusta a aquellos con más de 60 días sin actualizar
     * @return Número de productos actualizados
     */
    @Transactional
    public int applyInflationAdjustment(BigDecimal percentage, boolean onlyIfDue) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El porcentaje de ajuste debe ser un valor positivo.");
        }

        List<Product> targets = onlyIfDue ? getProductsRequiringPriceUpdate() : productRepository.findAllByDeletedFalse();
        BigDecimal factor = BigDecimal.ONE.add(percentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));

        int updatedCount = 0;
        for (Product product : targets) {
            Optional<PriceHistory> currentPriceOpt = getCurrentPrice(product.getId());
            BigDecimal current = currentPriceOpt.map(ph -> ph.getPrice()).orElse(product.getCurrentPrice());
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal updatedPrice = current.multiply(factor).setScale(2, RoundingMode.HALF_UP);
                updateProductPrice(product.getId(), updatedPrice, null);
                updatedCount++;
            }
        }
        return updatedCount;
    }
}
