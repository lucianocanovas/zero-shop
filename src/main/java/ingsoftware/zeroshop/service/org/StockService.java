package ingsoftware.zeroshop.service.org;

import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.service.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    public static final int CRITICAL_STOCK_THRESHOLD = 5;

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final OfficeRepository officeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public StockService(StockRepository stockRepository,
                        ProductRepository productRepository,
                        OfficeRepository officeRepository,
                        @Autowired(required = false) UserRepository userRepository,
                        @Autowired(required = false) EmailService emailService) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.officeRepository = officeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public StockService(StockRepository stockRepository,
                        ProductRepository productRepository,
                        OfficeRepository officeRepository) {
        this(stockRepository, productRepository, officeRepository, null, null);
    }

    public List<Stock> getAllActiveStock() {
        return stockRepository.findAllByDeletedFalse();
    }

    public List<Stock> getStockByOffice(UUID officeId) {
        return stockRepository.findByOfficeIdAndDeletedFalse(officeId);
    }

    public List<Stock> getStockByProduct(UUID productId) {
        return stockRepository.findByProductIdAndDeletedFalse(productId);
    }

    public Integer getTotalStockForProduct(UUID productId) {
        return stockRepository.getTotalQuantityByProductId(productId);
    }

    public Optional<Stock> getStock(UUID productId, UUID officeId) {
        return stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId);
    }

    public int getProductStockInOffice(UUID productId, UUID officeId) {
        if (productId == null || officeId == null) {
            return 0;
        }
        return getStock(productId, officeId)
            .map(stock -> stock.getQuantity() != null ? stock.getQuantity() : 0)
            .orElse(0);
    }

    public Stock getStockById(UUID id) {
        return stockRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de stock no encontrado con ID: " + id));
    }

    public boolean hasAvailableStock(UUID productId, UUID officeId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        return stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId)
                .map(stock -> stock.getQuantity() >= quantity)
                .orElse(false);
    }

    @Transactional
    public void decrementStock(UUID productId, UUID officeId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return;
        }
        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId)
                .orElseThrow(() -> new IllegalStateException("No existe registro de stock para el producto en la sucursal seleccionada."));

        if (stock.getQuantity() < quantity) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + stock.getProduct().getName());
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        Stock saved = stockRepository.save(stock);
        checkAndNotifyCriticalStock(saved);
    }

    @Transactional
    public void incrementStock(UUID productId, UUID officeId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return;
        }
        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId)
                .orElseGet(() -> {
                    Product product = productRepository.findActive(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));
                    Office office = officeRepository.findActive(officeId)
                            .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + officeId));
                    return Stock.builder()
                            .product(product)
                            .office(office)
                            .quantity(0)
                            .deleted(false)
                            .build();
                });

        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
    }

    @Transactional
    public Stock updateStockQuantity(UUID stockId, Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("La cantidad de stock no puede ser negativa.");
        }
        Stock stock = getStockById(stockId);
        stock.setQuantity(newQuantity);
        Stock saved = stockRepository.save(stock);
        checkAndNotifyCriticalStock(saved);
        return saved;
    }

    @Transactional
    public Stock adjustStock(UUID stockId, Integer adjustment) {
        if (adjustment == null) {
            throw new IllegalArgumentException("El valor de ajuste no puede ser nulo.");
        }
        Stock stock = getStockById(stockId);
        int updated = stock.getQuantity() + adjustment;
        if (updated < 0) {
            throw new IllegalStateException("El ajuste resultaría en un stock negativo (" + updated + ").");
        }
        stock.setQuantity(updated);
        Stock saved = stockRepository.save(stock);
        checkAndNotifyCriticalStock(saved);
        return saved;
    }

    @Transactional
    public Stock setStock(UUID productId, UUID officeId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("La cantidad de stock no puede ser negativa.");
        }
        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId)
                .orElseGet(() -> {
                    Product product = productRepository.findActive(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));
                    Office office = officeRepository.findActive(officeId)
                            .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + officeId));
                    return Stock.builder()
                            .product(product)
                            .office(office)
                            .quantity(0)
                            .deleted(false)
                            .build();
                });

        stock.setQuantity(quantity);
        Stock saved = stockRepository.save(stock);
        checkAndNotifyCriticalStock(saved);
        return saved;
    }

    private void checkAndNotifyCriticalStock(Stock stock) {
        if (stock == null || stock.getQuantity() == null || stock.getQuantity() > CRITICAL_STOCK_THRESHOLD) {
            return;
        }

        if (emailService == null || userRepository == null) {
            return;
        }

        try {
            List<User> admins = userRepository.findByRoleAndDeletedFalse(Role.ADMIN);
            if (admins == null || admins.isEmpty()) {
                return;
            }

            String productName = stock.getProduct() != null ? stock.getProduct().getName() : "Producto";
            String productCode = stock.getProduct() != null ? stock.getProduct().getCode() : "";
            String officeName = stock.getOffice() != null ? stock.getOffice().getName() : "Depósito";
            int currentStock = stock.getQuantity();

            for (User admin : admins) {
                String email = null;
                if (admin.getUsername() != null && admin.getUsername().contains("@")) {
                    email = admin.getUsername();
                } else if (admin.getPerson() != null && admin.getPerson().getContact() != null) {
                    email = admin.getPerson().getContact().stream()
                            .filter(c -> c instanceof ContactEmail)
                            .map(c -> ((ContactEmail) c).getEmail())
                            .findFirst().orElse(null);
                }

                if (email != null && !email.isBlank()) {
                    emailService.sendCriticalStockAlertEmail(email, productName, productCode, currentStock, officeName);
                }
            }
        } catch (Exception e) {
            log.warn("Error al enviar alerta de stock crítico para producto {}: {}",
                    stock.getProduct() != null ? stock.getProduct().getId() : "desconocido", e.getMessage());
        }
    }
}
