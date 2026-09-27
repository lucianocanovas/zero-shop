package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class OfficeInitializer implements ApplicationRunner {

    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public OfficeInitializer(OfficeRepository officeRepository,
                             ProductRepository productRepository,
                             StockRepository stockRepository) {
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeOfficesAndStock();
    }

    private void initializeOfficesAndStock() {
        if (officeRepository.findAllByDeletedFalse().isEmpty()) {
            Office central = new Office();
            central.setName("Casa Central Mendoza (Av. San Martín)");
            central.setCuit("30-71234567-8");
            central.setType(OfficeType.HEADQUARTERS);
            central.setDeleted(false);
            central = officeRepository.save(central);

            Office palmares = new Office();
            palmares.setName("Sucursal Palmares (Godoy Cruz)");
            palmares.setCuit("30-71234567-9");
            palmares.setType(OfficeType.BRANCH);
            palmares.setDeleted(false);
            palmares = officeRepository.save(palmares);

            List<Product> products = productRepository.findAllByDeletedFalse();
            for (Product product : products) {
                if (stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(product.getId(), central.getId()).isEmpty()) {
                    Stock stockCentral = Stock.builder()
                            .product(product)
                            .office(central)
                            .quantity(30)
                            .deleted(false)
                            .build();
                    stockRepository.save(stockCentral);
                }

                if (stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(product.getId(), palmares.getId()).isEmpty()) {
                    Stock stockPalmares = Stock.builder()
                            .product(product)
                            .office(palmares)
                            .quantity(20)
                            .deleted(false)
                            .build();
                    stockRepository.save(stockPalmares);
                }
            }
        }
    }
}
