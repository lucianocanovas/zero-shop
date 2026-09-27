package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.PaymentService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.junit.jupiter.api.BeforeEach;
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
public class StockAndCashPaymentIntegrationTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private OfficeRepository officeRepository;

    private Product testProduct;
    private Office testOffice;
    private final String clientUsername = "cashclient@test.com";

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsernameIgnoreCaseAndDeletedFalse(clientUsername).isEmpty()) {
            User user = new User();
            user.setUsername(clientUsername);
            user.setPassword("secret123");
            user.setRole(Role.CLIENT);
            user.setDeleted(false);
            userRepository.save(user);
        }

        testOffice = new Office();
        testOffice.setName("Sucursal Efectivo " + UUID.randomUUID().toString().substring(0, 5));
        testOffice.setCuit("30-" + UUID.randomUUID().toString().substring(0, 8) + "-1");
        testOffice.setType(OfficeType.BRANCH);
        testOffice.setDeleted(false);
        testOffice = officeRepository.save(testOffice);

        Category category = Category.builder()
                .name("Cat Stock " + UUID.randomUUID().toString().substring(0, 5))
                .description("Test Category")
                .deleted(false)
                .build();
        category = categoryRepository.save(category);

        SubCategory subCategory = SubCategory.builder()
                .name("SubCat Stock " + UUID.randomUUID().toString().substring(0, 5))
                .category(category)
                .deleted(false)
                .build();
        subCategory = subCategoryRepository.save(subCategory);

        testProduct = Product.builder()
                .name("Producto Stock Test " + UUID.randomUUID().toString().substring(0, 5))
                .code("STK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .size(Size.L)
                .currentPrice(new BigDecimal("18500.00"))
                .subCategory(subCategory)
                .deleted(false)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("StockService: Asignar, ajustar, actualizar y consultar stock")
    void testStockServiceOperations() {
        // 1. Asignar stock inicial
        Stock stock = stockService.setStock(testProduct.getId(), testOffice.getId(), 20);
        assertNotNull(stock);
        assertNotNull(stock.getId());
        assertEquals(20, stock.getQuantity());

        // 2. Incrementar stock
        stockService.incrementStock(testProduct.getId(), testOffice.getId(), 10);
        assertEquals(30, stockService.getStock(testProduct.getId(), testOffice.getId()).get().getQuantity());

        // 3. Ajuste manual (+5)
        Stock adjusted = stockService.adjustStock(stock.getId(), 5);
        assertEquals(35, adjusted.getQuantity());

        // 4. Ajuste manual negativo (-15)
        Stock decremented = stockService.adjustStock(stock.getId(), -15);
        assertEquals(20, decremented.getQuantity());

        // 5. Fijar cantidad directa
        Stock updated = stockService.updateStockQuantity(stock.getId(), 50);
        assertEquals(50, updated.getQuantity());

        // 6. Verificar disponibilidad
        assertTrue(stockService.hasAvailableStock(testProduct.getId(), testOffice.getId(), 50));
        assertFalse(stockService.hasAvailableStock(testProduct.getId(), testOffice.getId(), 51));

        // 7. No permitir ajuste a negativo
        assertThrows(IllegalStateException.class, () -> {
            stockService.adjustStock(stock.getId(), -100);
        });
    }

    @Test
    @DisplayName("Pago en Efectivo (CASH): Checkout en efectivo y cobro con payOrderWithCash descuenta stock")
    void testCashPaymentFlowWithStockDecrement() {
        // 1. Inicializamos stock con 15 unidades
        stockService.setStock(testProduct.getId(), testOffice.getId(), 15);

        // 2. Cliente agrega 3 unidades al carrito
        saleOrderService.addProductToCart(clientUsername, testProduct.getId(), 3);

        // 3. Procesa checkout con método CASH
        String redirectUrl = saleOrderService.processCheckout(
                clientUsername,
                testOffice.getId(),
                "San Martín",
                "1500",
                null,
                null,
                "5500",
                "Mendoza",
                "+54 9 261 444-5566",
                PaymentMethod.CASH
        );
        assertTrue(redirectUrl.contains("/checkout/success"));

        // Obtenemos la orden creada
        List<SaleOrder> orders = saleOrderService.getClientOrders(clientUsername);
        assertFalse(orders.isEmpty());
        SaleOrder cashOrder = orders.get(0);

        assertEquals(OrderStatus.PENDING_PAYMENT, cashOrder.getStatus());

        // El stock aún permanece intacto (15 unidades) porque está pendiente de pago
        assertEquals(15, stockService.getStock(testProduct.getId(), testOffice.getId()).get().getQuantity());

        // 4. Registro de cobro en efectivo mediante payOrderWithCash
        SaleOrder paidOrder = saleOrderService.payOrderWithCash(cashOrder.getId());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        // 5. El stock debe haber disminuido exactamente en 3 unidades (15 - 3 = 12)
        int finalStock = stockService.getStock(testProduct.getId(), testOffice.getId()).get().getQuantity();
        assertEquals(12, finalStock, "El stock debe disminuir en 3 unidades al registrar el pago en efectivo");

        // 6. Validar que exista el registro de pago asociado
        List<Payment> payments = paymentService.getPaymentsByOrder(cashOrder.getId());
        assertFalse(payments.isEmpty());
        assertEquals(PaymentMethod.CASH, payments.get(0).getMethod());
        assertEquals(cashOrder.getTotalAmount(), payments.get(0).getAmount());
    }
}
