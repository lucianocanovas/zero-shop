package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Unit: Crear producto persiste la entidad y crea registro de PriceHistory inicial")
    public void testCreateProductSuccess() {
        UUID subCatId = UUID.randomUUID();
        SubCategory subCategory = SubCategory.builder()
                .id(subCatId)
                .name("Calzado")
                .category(Category.builder().name("Hombres").build())
                .build();

        Product product = Product.builder()
                .code("RUN-100")
                .name("Zapatillas Zero Runner")
                .description("Calzado para maratón")
                .size(Size.XL)
                .onSale(false)
                .build();

        when(subCategoryRepository.findActive(subCatId)).thenReturn(Optional.of(subCategory));
        when(productRepository.findByCodeAndDeletedFalse("RUN-100")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        BigDecimal price = new BigDecimal("49999.00");
        Product created = productService.createProduct(product, price, subCatId);

        assertNotNull(created);
        assertEquals("RUN-100", created.getCode());
        assertEquals("Zapatillas Zero Runner", created.getName());
        assertEquals(subCategory, created.getSubCategory());
        verify(priceHistoryRepository, times(1)).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("Unit: findOnSaleProducts retorna productos en oferta enriquecidos con stock y precio")
    public void testFindOnSaleProducts() {
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        Product offer1 = Product.builder()
                .id(p1Id)
                .name("Campera en oferta")
                .onSale(true)
                .deleted(false)
                .build();

        Product offer2 = Product.builder()
                .id(p2Id)
                .name("Gorra en oferta")
                .onSale(true)
                .deleted(false)
                .build();

        when(productRepository.findByOnSaleTrueAndDeletedFalse()).thenReturn(List.of(offer1, offer2));
        when(stockRepository.getTotalQuantityByProductId(p1Id)).thenReturn(10);
        when(stockRepository.getTotalQuantityByProductId(p2Id)).thenReturn(0);
        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(any()))
                .thenReturn(Optional.empty());

        List<Product> result = productService.findOnSaleProducts();

        assertEquals(2, result.size());
        assertEquals("Campera en oferta", result.get(0).getName());
        assertEquals(10, result.get(0).getStock());
        assertEquals(0, result.get(1).getStock());
    }

    @Test
    @DisplayName("Unit: searchProducts filtra correctamente por texto, categoría y precio máximo")
    public void testSearchProductsFilter() {
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        Category catHombres = Category.builder().id(UUID.randomUUID()).name("Hombres").build();
        SubCategory subCalzado = SubCategory.builder().name("Calzado").category(catHombres).build();

        Product p1 = Product.builder()
                .id(p1Id)
                .code("BOT-01")
                .name("Botines Predator Zero")
                .subCategory(subCalzado)
                .deleted(false)
                .build();

        Product p2 = Product.builder()
                .id(p2Id)
                .code("MED-01")
                .name("Medias Running")
                .subCategory(subCalzado)
                .deleted(false)
                .build();

        when(productRepository.findAllByDeletedFalse()).thenReturn(List.of(p1, p2));
        when(stockRepository.getTotalQuantityByProductId(any())).thenReturn(5);

        PriceHistory ph1 = PriceHistory.builder().price(new BigDecimal("30000.00")).build();
        PriceHistory ph2 = PriceHistory.builder().price(new BigDecimal("5000.00")).build();

        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(p1Id))
                .thenReturn(Optional.of(ph1));
        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(p2Id))
                .thenReturn(Optional.of(ph2));

        // Búsqueda por texto "botines"
        List<Product> textMatch = productService.searchProducts("botines", null, null, null);
        assertEquals(1, textMatch.size());
        assertEquals("Botines Predator Zero", textMatch.get(0).getName());

        // Búsqueda por precio máximo (10000.00) -> Solo p2 (5000.00) debe pasar
        List<Product> priceMatch = productService.searchProducts(null, null, null, new BigDecimal("10000.00"));
        assertEquals(1, priceMatch.size());
        assertEquals("Medias Running", priceMatch.get(0).getName());
    }
}
