package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
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
public class CheckoutAndPaymentFlowIntegrationTest {

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OfficeRepository officeRepository;

    private Product product1;
    private Product product2;
    private Office office;
    private final String clientUsername = "client@gmail.com";

    @BeforeEach
    public void setUp() {
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        if (offices.isEmpty()) {
            Office newOffice = new Office();
            newOffice.setName("Sucursal Central Mendoza");
            newOffice.setCuit("30-12345678-9");
            newOffice.setType(OfficeType.HEADQUARTERS);
            newOffice.setDeleted(false);
            office = officeRepository.save(newOffice);
        } else {
            office = offices.get(0);
        }

        List<Product> products = productRepository.findAllByDeletedFalse();
        if (products.size() < 2) {
            product1 = productService.createProduct(
                    Product.builder()
                            .code("TEST-PROD-1-" + UUID.randomUUID().toString().substring(0, 5))
                            .name("Zapatilla Running Pro")
                            .size(Size.M)
                            .build(),
                    new BigDecimal("25000.00"),
                    null
            );
            product2 = productService.createProduct(
                    Product.builder()
                            .code("TEST-PROD-2-" + UUID.randomUUID().toString().substring(0, 5))
                            .name("Remera Deportiva DryFit")
                            .size(Size.L)
                            .build(),
                    new BigDecimal("12000.00"),
                    null
            );
        } else {
            product1 = products.get(0);
            product2 = products.get(1);
        }

        // Aseguramos stock suficiente en la sucursal para las pruebas
        stockService.incrementStock(product1.getId(), office.getId(), 50);
        stockService.incrementStock(product2.getId(), office.getId(), 50);

        // Limpiar ítems previos del carrito para asegurar aislamiento
        SaleOrder cart = saleOrderService.getOrCreateCart(clientUsername);
        List<OrderDetail> existingDetails = saleOrderService.getOrderDetails(cart.getId());
        for (OrderDetail detail : existingDetails) {
            saleOrderService.removeCartItem(clientUsername, detail.getId());
        }
    }

    @Test
    @DisplayName("Gestión del Carrito: Agregar, actualizar cantidad y remover ítems")
    public void testCartOperations() {
        // 1. Agregar product1 con cantidad 2
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product1.getId(), 2);
        assertNotNull(cart);
        assertEquals(OrderStatus.ON_CART, cart.getStatus());

        List<OrderDetail> details = saleOrderService.getOrderDetails(cart.getId());
        assertEquals(1, details.size());
        assertEquals(2, details.get(0).getQuantity());

        // 2. Agregar product2 con cantidad 1
        saleOrderService.addProductToCart(clientUsername, product2.getId(), 1);
        details = saleOrderService.getOrderDetails(cart.getId());
        assertEquals(2, details.size());

        // 3. Modificar cantidad de product1 a 4
        OrderDetail detail1 = details.stream().filter(d -> d.getProduct().getId().equals(product1.getId())).findFirst().orElseThrow();
        saleOrderService.updateCartItemQuantity(clientUsername, detail1.getId(), 4);
        details = saleOrderService.getOrderDetails(cart.getId());
        OrderDetail updatedDetail1 = details.stream().filter(d -> d.getId().equals(detail1.getId())).findFirst().orElseThrow();
        assertEquals(4, updatedDetail1.getQuantity());

        // 4. Remover product2 del carrito
        OrderDetail detail2 = details.stream().filter(d -> d.getProduct().getId().equals(product2.getId())).findFirst().orElseThrow();
        saleOrderService.removeCartItem(clientUsername, detail2.getId());
        details = saleOrderService.getOrderDetails(cart.getId());
        assertEquals(1, details.size());
        assertEquals(product1.getId(), details.get(0).getProduct().getId());
    }

    @Test
    @DisplayName("Flujo de Compra con EFECTIVO (CASH): Orden pendiente de pago, registro de pago y posterior despacho")
    public void testPurchaseFlowWithCash() {
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product1.getId(), 2);
        BigDecimal expectedTotal = cart.getTotalAmount();
        int initialStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);

        // Checkout en efectivo
        String redirectUrl = saleOrderService.processCheckout(
                clientUsername,
                office.getId(),
                "San Martín",
                "1000",
                "1",
                "A",
                "5500",
                "Mendoza Capital",
                "+54 9 261 411-2233",
                PaymentMethod.CASH
        );

        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("/checkout/success"));

        // Verificar estado de orden PENDING_PAYMENT
        SaleOrder order = saleOrderService.getOrderById(cart.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(expectedTotal, order.getTotalAmount());
        assertNotNull(order.getShippingAddress());

        // Verificar registro de pago asociado
        List<Payment> payments = paymentService.getPaymentsByOrder(order.getId());
        assertFalse(payments.isEmpty(), "Debe existir un pago registrado");
        Payment payment = payments.get(0);
        assertEquals(PaymentMethod.CASH, payment.getMethod());
        assertEquals(expectedTotal, payment.getAmount());

        // Stock no se descuenta hasta que se confirme el pago en efectivo
        int currentStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);
        assertEquals(initialStock, currentStock, "El stock no debe descontarse antes de cobrar el efectivo");

        // Empleado cobra el pedido -> cambia estado a PAID
        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
        order = saleOrderService.getOrderById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, order.getStatus());

        // Ahora sí se descuenta el stock
        int finalStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);
        assertEquals(initialStock - 2, finalStock, "El stock debe disminuir en 2 unidades");

        // Transiciones adicionales del ciclo de vida del pedido
        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.PENDING_SHIPPING);
        assertEquals(OrderStatus.PENDING_SHIPPING, saleOrderService.getOrderById(order.getId()).orElseThrow().getStatus());

        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, saleOrderService.getOrderById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Flujo de Compra con TARJETA DE CRÉDITO: Pago directo, estado PAID y descuento de stock inmediato")
    public void testPurchaseFlowWithCreditCard() {
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product1.getId(), 3);
        BigDecimal expectedTotal = cart.getTotalAmount();
        int initialStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);

        String redirectUrl = saleOrderService.processCheckout(
                clientUsername,
                office.getId(),
                "Belgrano",
                "500",
                null,
                null,
                "5500",
                "Ciudad",
                "+54 9 261 555-9988",
                PaymentMethod.CREDIT
        );

        assertTrue(redirectUrl.contains("/checkout/success"));

        SaleOrder order = saleOrderService.getOrderById(cart.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, order.getStatus(), "Con tarjeta de crédito la orden pasa directamente a PAID");

        List<Payment> payments = paymentService.getPaymentsByOrder(order.getId());
        assertFalse(payments.isEmpty());
        assertEquals(PaymentMethod.CREDIT, payments.get(0).getMethod());
        assertEquals(expectedTotal, payments.get(0).getAmount());

        // Stock descontado inmediatamente
        int finalStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);
        assertEquals(initialStock - 3, finalStock, "El stock debe descontarse inmediatamente");
    }

    @Test
    @DisplayName("Flujo de Compra con TARJETA DE DÉBITO: Pago directo, método DEBIT y descuento de stock")
    public void testPurchaseFlowWithDebitCard() {
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product2.getId(), 1);
        BigDecimal expectedTotal = cart.getTotalAmount();
        int initialStock = stockService.getStock(product2.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);

        String redirectUrl = saleOrderService.processCheckout(
                clientUsername,
                office.getId(),
                "Las Heras",
                "250",
                null,
                null,
                "5500",
                "Mendoza",
                "+54 9 261 333-4455",
                PaymentMethod.DEBIT
        );

        assertTrue(redirectUrl.contains("/checkout/success"));

        SaleOrder order = saleOrderService.getOrderById(cart.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, order.getStatus());

        List<Payment> payments = paymentService.getPaymentsByOrder(order.getId());
        assertFalse(payments.isEmpty());
        assertEquals(PaymentMethod.DEBIT, payments.get(0).getMethod());
        assertEquals(expectedTotal, payments.get(0).getAmount());

        int finalStock = stockService.getStock(product2.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);
        assertEquals(initialStock - 1, finalStock);
    }

    @Test
    @DisplayName("Flujo de Compra con MERCADO PAGO: Invocación de SDK para crear preferencia y posterior confirmación de pago")
    public void testPurchaseFlowWithMercadoPago() {
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product1.getId(), 2);
        int initialStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);

        // Checkout solicitando Mercado Pago como medio de pago
        String redirectUrl = saleOrderService.processCheckout(
                clientUsername,
                office.getId(),
                "Sarmiento",
                "777",
                "Piso 4",
                "Dpto 12",
                "5500",
                "Mendoza",
                "+54 9 261 999-8877",
                PaymentMethod.MERCADO_PAGO
        );

        // La URL de redirección debe apuntar o a la pasarela de MP o al flujo de retorno controlado
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("mercadopago.com") || redirectUrl.contains("/checkout/mp/success"),
                "Debe generar un enlace de checkout de Mercado Pago o fallback");

        // Al retornar de Mercado Pago tras completar el pago (Callback / Webhook):
        SaleOrder paidOrder = saleOrderService.handleMercadoPagoSuccess(cart.getId());
        assertNotNull(paidOrder);
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        // Verificar registro de pago con MERCADO_PAGO
        List<Payment> payments = paymentService.getPaymentsByOrder(paidOrder.getId());
        assertFalse(payments.isEmpty());
        assertEquals(PaymentMethod.MERCADO_PAGO, payments.get(0).getMethod());

        // Verificar descuento de stock en la sucursal
        int finalStock = stockService.getStock(product1.getId(), office.getId()).map(ingsoftware.zeroshop.entity.org.Stock::getQuantity).orElse(0);
        assertEquals(initialStock - 2, finalStock, "El stock debe descontarse tras el pago por Mercado Pago");

        // El cliente debe ver la orden en su lista de compras confirmadas
        List<SaleOrder> clientOrders = saleOrderService.getClientOrders(clientUsername);
        assertTrue(clientOrders.stream().anyMatch(o -> o.getId().equals(paidOrder.getId())));
    }
}
