package ingsoftware.zeroshop.service.catalog;

import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StockRepository stockRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ProductService(ProductRepository productRepository,
                          PriceHistoryRepository priceHistoryRepository,
                          StockRepository stockRepository,
                          SubCategoryRepository subCategoryRepository) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.stockRepository = stockRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        List<Product> products = productRepository.findAllByDeletedFalse();
        for (Product product : products) {
            enrichProductData(product);
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String search, String categoryId, String subCategory, BigDecimal maxPrice) {
        List<Product> products = findAllActive();

        return products.stream()
            .filter(p -> {
                // Search filter (text in name, code, description, subcategory, category)
                if (search != null && !search.isBlank()) {
                    String query = search.trim().toLowerCase();
                    boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(query);
                    boolean matchCode = p.getCode() != null && p.getCode().toLowerCase().contains(query);
                    boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(query);
                    boolean matchSub = p.getSubCategory() != null && p.getSubCategory().getName() != null && p.getSubCategory().getName().toLowerCase().contains(query);
                    boolean matchCat = p.getCategory() != null && p.getCategory().getName() != null && p.getCategory().getName().toLowerCase().contains(query);
                    if (!matchName && !matchCode && !matchDesc && !matchSub && !matchCat) {
                        return false;
                    }
                }

                // Category filter (UUID string or name e.g. "hombres", "mujeres")
                if (categoryId != null && !categoryId.isBlank()) {
                    String catFilter = categoryId.trim();
                    if (p.getCategory() == null) {
                        return false;
                    }
                    boolean matches = false;
                    if (p.getCategory().getId() != null && p.getCategory().getId().toString().equalsIgnoreCase(catFilter)) {
                        matches = true;
                    } else if (p.getCategory().getName() != null) {
                        String catName = p.getCategory().getName().toLowerCase();
                        String f = catFilter.toLowerCase();
                        if (catName.equalsIgnoreCase(f) ||
                            (f.equals("ninos") && catName.contains("niño")) ||
                            (f.equals("ninas") && catName.contains("niña"))) {
                            matches = true;
                        }
                    }
                    if (!matches) {
                        return false;
                    }
                }

                // SubCategory filter (e.g. "calzado", "ropa" / "indumentaria", "accesorios")
                if (subCategory != null && !subCategory.isBlank()) {
                    if (p.getSubCategory() == null || p.getSubCategory().getName() == null) {
                        return false;
                    }
                    String subFilter = subCategory.trim().toLowerCase();
                    String pSubName = p.getSubCategory().getName().toLowerCase();
                    if (subFilter.equals("ropa")) {
                        if (!pSubName.contains("indumentaria") && !pSubName.contains("ropa")) {
                            return false;
                        }
                    } else if (!pSubName.contains(subFilter)) {
                        return false;
                    }
                }

                // Max price filter
                if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                    if (p.getCurrentPrice() == null || p.getCurrentPrice().compareTo(maxPrice) > 0) {
                        return false;
                    }
                }

                return true;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public Product findActiveById(UUID id) {
        Product product = productRepository.findActive(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        enrichProductData(product);
        return product;
    }

    public Product createProduct(Product product, BigDecimal basePrice, UUID subCategoryId) {
        if (product.getName() == null || product.getName().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (product.getCode() == null || product.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("El código SKU no puede estar vacío");
        }
        if (product.getSize() == null) {
            throw new IllegalArgumentException("Debe seleccionar un talle para el producto");
        }
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }

        String trimmedCode = product.getCode().trim();
        product.setCode(trimmedCode);
        product.setName(product.getName().trim());

        if (productRepository.findByCodeAndDeletedFalse(trimmedCode).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el código SKU: " + trimmedCode);
        }

        if (subCategoryId != null) {
            SubCategory subCategory = subCategoryRepository.findActive(subCategoryId).orElse(null);
            product.setSubCategory(subCategory);
        }

        if (product.getOnSale() == null) {
            product.setOnSale(false);
        }
        product.setDeleted(false);
        Product savedProduct = productRepository.save(product);

        if (basePrice != null) {
            PriceHistory priceHistory = PriceHistory.builder()
                .product(savedProduct)
                .price(basePrice)
                .startDate(LocalDateTime.now())
                .deleted(false)
                .build();
            priceHistoryRepository.save(priceHistory);
            savedProduct.setCurrentPrice(basePrice);
        } else {
            savedProduct.setCurrentPrice(BigDecimal.ZERO);
        }

        savedProduct.setStock(0);
        return savedProduct;
    }

    public Product updateProduct(UUID id, Product formProduct, BigDecimal basePrice, UUID subCategoryId) {
        Product existing = findActiveById(id);

        if (formProduct.getName() == null || formProduct.getName().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (formProduct.getCode() == null || formProduct.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("El código SKU no puede estar vacío");
        }
        if (formProduct.getSize() == null) {
            throw new IllegalArgumentException("Debe seleccionar un talle para el producto");
        }
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }

        String newCode = formProduct.getCode().trim();
        if (!existing.getCode().equalsIgnoreCase(newCode)) {
            Optional<Product> duplicate = productRepository.findByCodeAndDeletedFalse(newCode);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro producto con el código SKU: " + newCode);
            }
            existing.setCode(newCode);
        }

        existing.setName(formProduct.getName().trim());
        existing.setDescription(formProduct.getDescription());
        existing.setSize(formProduct.getSize());
        existing.setImageUrl(formProduct.getImageUrl());
        if (formProduct.getOnSale() != null) {
            existing.setOnSale(formProduct.getOnSale());
        }

        if (subCategoryId != null) {
            SubCategory subCategory = subCategoryRepository.findActive(subCategoryId).orElse(null);
            existing.setSubCategory(subCategory);
        } else {
            existing.setSubCategory(null);
        }

        if (basePrice != null) {
            Optional<PriceHistory> currentPriceOpt = priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(id);
            if (currentPriceOpt.isEmpty() || currentPriceOpt.get().getPrice().compareTo(basePrice) != 0) {
                currentPriceOpt.ifPresent(p -> {
                    p.setEndDate(LocalDateTime.now());
                    priceHistoryRepository.save(p);
                });

                PriceHistory newPrice = PriceHistory.builder()
                    .product(existing)
                    .price(basePrice)
                    .startDate(LocalDateTime.now())
                    .deleted(false)
                    .build();
                priceHistoryRepository.save(newPrice);
                existing.setCurrentPrice(basePrice);
            }
        }

        return productRepository.save(existing);
    }

    public void deleteProduct(UUID id) {
        Product existing = findActiveById(id);
        existing.setDeleted(true);
        productRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<PriceHistory> getProductPriceHistory(UUID productId) {
        return priceHistoryRepository.findByProductIdAndDeletedFalseOrderByStartDateDesc(productId);
    }

    public PriceHistory addProductPrice(UUID productId, BigDecimal price) {
        Product product = findActiveById(productId);
        Optional<PriceHistory> currentPriceOpt = priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId);
        currentPriceOpt.ifPresent(p -> {
            p.setEndDate(LocalDateTime.now());
            priceHistoryRepository.save(p);
        });

        PriceHistory newPrice = PriceHistory.builder()
            .product(product)
            .price(price)
            .startDate(LocalDateTime.now())
            .deleted(false)
            .build();
        return priceHistoryRepository.save(newPrice);
    }

    private void enrichProductData(Product product) {
        Optional<PriceHistory> priceHistory = priceHistoryRepository
            .findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(product.getId());
        product.setCurrentPrice(priceHistory.map(PriceHistory::getPrice).orElse(BigDecimal.ZERO));

        Integer totalStock = stockRepository.getTotalQuantityByProductId(product.getId());
        product.setStock(totalStock != null ? totalStock : 0);
    }

}
