package ingsoftware.zeroshop.service.org;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final OfficeRepository officeRepository;

    public StockService(StockRepository stockRepository,
                        ProductRepository productRepository,
                        OfficeRepository officeRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.officeRepository = officeRepository;
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
        stockRepository.save(stock);
    }

    @Transactional
    public void incrementStock(UUID productId, UUID officeId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return;
        }
        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId)
                .orElseGet(() -> {
                    Product product = productRepository.findActive(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
                    Office office = officeRepository.findActive(officeId)
                            .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada."));
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
}
