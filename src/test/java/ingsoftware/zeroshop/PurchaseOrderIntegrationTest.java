package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.PurchaseOrder;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.PurchaseOrderService;
import org.junit.jupiter.api.BeforeEach;
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
public class PurchaseOrderIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private StockService stockService;

    private Supplier testSupplier;
    private Office testOffice;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setName("Distribuidora Test " + UUID.randomUUID().toString().substring(0, 6));
        testSupplier.setCuit("30-71" + UUID.randomUUID().toString().substring(0, 6) + "-9");
        testSupplier.setDeleted(false);
        testSupplier = supplierRepository.save(testSupplier);

        testOffice = new Office();
        testOffice.setName("Sucursal Test " + UUID.randomUUID().toString().substring(0, 6));
        testOffice.setCuit("20-" + UUID.randomUUID().toString().substring(0, 8) + "-5");
        testOffice.setType(OfficeType.BRANCH);
        testOffice.setDeleted(false);
        testOffice = officeRepository.save(testOffice);

        Category category = Category.builder()
                .name("Calzado Test " + UUID.randomUUID().toString().substring(0, 6))
                .description("Categoria de prueba")
                .deleted(false)
                .build();
        category = categoryRepository.save(category);

        SubCategory subCategory = SubCategory.builder()
                .name("Deportivo Test " + UUID.randomUUID().toString().substring(0, 6))
                .category(category)
                .deleted(false)
                .build();
        subCategory = subCategoryRepository.save(subCategory);

        testProduct = Product.builder()
                .name("Zapatillas Test " + UUID.randomUUID().toString().substring(0, 6))
                .description("Descripcion de prueba")
                .code("COD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .size(Size.M)
                .currentPrice(new BigDecimal("15000.00"))
                .subCategory(subCategory)
                .deleted(false)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testCreatePurchaseOrderSuccessfully() {
        int quantity = 25;
        BigDecimal unitPrice = new BigDecimal("8500.00");

        PurchaseOrder order = purchaseOrderService.createPurchaseOrder(
                testSupplier.getId(),
                testOffice.getId(),
                testProduct.getId(),
                quantity,
                unitPrice,
                null
        );

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.PENDING_DELIVERY, order.getStatus());
        assertEquals(unitPrice.multiply(BigDecimal.valueOf(quantity)), order.getTotalAmount());
        assertEquals(testSupplier.getId(), order.getSupplier().getId());
        assertEquals(testOffice.getId(), order.getOffice().getId());

        List<OrderDetail> details = purchaseOrderService.getOrderDetails(order.getId());
        assertEquals(1, details.size());
        assertEquals(testProduct.getId(), details.get(0).getProduct().getId());
        assertEquals(quantity, details.get(0).getQuantity());
        assertEquals(unitPrice, details.get(0).getUnitPrice());
    }

    @Test
    void testReceivePurchaseOrderIncrementsStock() {
        int initialStock = stockService.getStock(testProduct.getId(), testOffice.getId())
                .map(s -> s.getQuantity())
                .orElse(0);

        int incomingQuantity = 30;
        BigDecimal unitPrice = new BigDecimal("7000.00");

        PurchaseOrder order = purchaseOrderService.createPurchaseOrder(
                testSupplier.getId(),
                testOffice.getId(),
                testProduct.getId(),
                incomingQuantity,
                unitPrice,
                null
        );

        // Al recibir la orden de compra
        PurchaseOrder receivedOrder = purchaseOrderService.receivePurchaseOrder(order.getId());
        assertEquals(OrderStatus.DELIVERED, receivedOrder.getStatus());

        // Comprobamos que el stock aumentó exactamente en 30 unidades
        int finalStock = stockService.getStock(testProduct.getId(), testOffice.getId())
                .map(s -> s.getQuantity())
                .orElse(0);

        assertEquals(initialStock + incomingQuantity, finalStock);

        // No se puede recibir dos veces
        assertThrows(IllegalStateException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(order.getId());
        });
    }

    @Test
    void testCancelPurchaseOrder() {
        PurchaseOrder order = purchaseOrderService.createPurchaseOrder(
                testSupplier.getId(),
                testOffice.getId(),
                testProduct.getId(),
                10,
                new BigDecimal("5000.00"),
                null
        );

        purchaseOrderService.cancelPurchaseOrder(order.getId());

        assertTrue(purchaseOrderService.getPurchaseOrderById(order.getId()).isEmpty());
    }
}
