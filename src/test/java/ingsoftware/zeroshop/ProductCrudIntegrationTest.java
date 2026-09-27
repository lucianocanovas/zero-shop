package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProductCrudIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Test
    @DisplayName("Carga de producto: Debe crear un producto exitosamente con SKU, precio e historial")
    public void testCreateProductSuccess() {
        SubCategory subCategory = subCategoryRepository.findAllWithCategoryByDeletedFalse().stream().findFirst().orElse(null);
        UUID subCategoryId = subCategory != null ? subCategory.getId() : null;

        String testSku = "TEST-SKU-" + UUID.randomUUID().toString().substring(0, 6);
        Product newProduct = Product.builder()
                .code(testSku)
                .name("Campera Deportiva Pro Windbreaker")
                .description("Campera resistente al agua y viento para running nocturno")
                .size(Size.L)
                .imageUrl("/assets/products/campera-pro.jpg")
                .build();

        BigDecimal initialPrice = new BigDecimal("45999.50");
        Product created = productService.createProduct(newProduct, initialPrice, subCategoryId);

        assertNotNull(created.getId(), "El producto creado debe tener un ID generado");
        assertEquals(testSku, created.getCode());
        assertEquals("Campera Deportiva Pro Windbreaker", created.getName());
        assertEquals(Size.L, created.getSize());
        assertEquals(initialPrice, created.getCurrentPrice());
        assertFalse(created.getDeleted());

        // Verificar persistencia en repositorio
        Product fetched = productService.findActiveById(created.getId());
        assertNotNull(fetched);
        assertEquals(initialPrice, fetched.getCurrentPrice());

        // Verificar historial de precios
        List<PriceHistory> priceHistories = productService.getProductPriceHistory(created.getId());
        assertFalse(priceHistories.isEmpty(), "Debe existir al menos un historial de precios");
        assertEquals(initialPrice, priceHistories.get(0).getPrice());
        assertNull(priceHistories.get(0).getEndDate(), "El precio actual no debe tener fecha de fin");
    }

    @Test
    @DisplayName("Carga de producto: Debe fallar si el código SKU ya existe")
    public void testCreateProductDuplicateSkuFails() {
        String duplicateSku = "TEST-DUP-" + UUID.randomUUID().toString().substring(0, 6);

        Product firstProduct = Product.builder()
                .code(duplicateSku)
                .name("Producto Original")
                .size(Size.M)
                .build();
        productService.createProduct(firstProduct, new BigDecimal("10000"), null);

        Product secondProduct = Product.builder()
                .code(duplicateSku)
                .name("Producto Duplicado")
                .size(Size.L)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(secondProduct, new BigDecimal("12000"), null)
        );
        assertTrue(ex.getMessage().contains("Ya existe un producto con el código SKU"));
    }

    @Test
    @DisplayName("Carga de producto: Validaciones de campos obligatorios (nombre, código, talle, precio negativo)")
    public void testCreateProductValidations() {
        // Nombre vacío
        Product p1 = Product.builder().code("SKU-1").name("").size(Size.M).build();
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(p1, BigDecimal.TEN, null));

        // Código vacío
        Product p2 = Product.builder().code(" ").name("Nombre").size(Size.M).build();
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(p2, BigDecimal.TEN, null));

        // Talle nulo
        Product p3 = Product.builder().code("SKU-3").name("Nombre").size(null).build();
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(p3, BigDecimal.TEN, null));

        // Precio negativo
        Product p4 = Product.builder().code("SKU-4").name("Nombre").size(Size.M).build();
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(p4, new BigDecimal("-10"), null));
    }

    @Test
    @DisplayName("Modificación de producto: Debe actualizar datos básicos y generar nuevo historial si cambia el precio")
    public void testUpdateProductDataAndPrice() {
        String sku = "TEST-UPD-" + UUID.randomUUID().toString().substring(0, 6);
        Product original = Product.builder()
                .code(sku)
                .name("Short Running Original")
                .description("Versión 1")
                .size(Size.S)
                .build();

        BigDecimal price1 = new BigDecimal("15000.00");
        Product created = productService.createProduct(original, price1, null);

        // Modificar datos y precio
        Product updateForm = Product.builder()
                .code(sku)
                .name("Short Running Ultra 2.0")
                .description("Versión actualizada con bolsillos con cierre")
                .size(Size.M)
                .imageUrl("/assets/products/short-2.0.jpg")
                .onSale(true)
                .build();

        BigDecimal price2 = new BigDecimal("18500.00");
        Product updated = productService.updateProduct(created.getId(), updateForm, price2, null);

        assertEquals("Short Running Ultra 2.0", updated.getName());
        assertEquals("Versión actualizada con bolsillos con cierre", updated.getDescription());
        assertEquals(Size.M, updated.getSize());
        assertEquals("/assets/products/short-2.0.jpg", updated.getImageUrl());
        assertTrue(updated.getOnSale());
        assertEquals(price2, updated.getCurrentPrice());

        // Verificar historial de precios: deben haber 2 registros, el anterior con endDate y el nuevo activo
        List<PriceHistory> histories = productService.getProductPriceHistory(created.getId());
        assertEquals(2, histories.size(), "Deben existir dos entradas de historial de precios");
        assertEquals(price2, histories.get(0).getPrice(), "El más reciente debe tener el nuevo precio");
        assertNull(histories.get(0).getEndDate(), "El más reciente no debe tener endDate");
        assertEquals(price1, histories.get(1).getPrice(), "El anterior debe tener el precio inicial");
        assertNotNull(histories.get(1).getEndDate(), "El precio anterior debe haber sido cerrado con endDate");
    }

    @Test
    @DisplayName("Baja de producto: Debe realizar baja lógica y excluirlo de listados activos y catálogo")
    public void testDeleteProductSoftDelete() {
        String sku = "TEST-DEL-" + UUID.randomUUID().toString().substring(0, 6);
        Product product = Product.builder()
                .code(sku)
                .name("Producto para Eliminar")
                .size(Size.XL)
                .build();
        Product created = productService.createProduct(product, new BigDecimal("9999.00"), null);
        UUID productId = created.getId();

        // Verificar que inicialmente está activo
        assertTrue(productService.findAllActive().stream().anyMatch(p -> p.getId().equals(productId)));

        // Ejecutar Baja del producto
        productService.deleteProduct(productId);

        // Verificar que findActiveById lanza excepción
        assertThrows(IllegalArgumentException.class, () -> productService.findActiveById(productId));

        // Verificar que no figura en findAllActive
        assertFalse(productService.findAllActive().stream().anyMatch(p -> p.getId().equals(productId)));

        // Verificar en búsqueda de catálogo que no aparece
        List<Product> searchResults = productService.searchProducts("Eliminar", null, null, null);
        assertFalse(searchResults.stream().anyMatch(p -> p.getId().equals(productId)));

        // Verificar a nivel entidad que deleted es true
        Product inDb = productRepository.findById(productId).orElseThrow();
        assertTrue(inDb.getDeleted(), "La propiedad deleted debe ser true");
    }
}
