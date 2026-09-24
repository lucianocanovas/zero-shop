package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PurchaseFlowIntegrationTest {

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private StockService stockService;

    @Test
    @Transactional
    public void testFullPurchaseFlow() {
        // 1. Verificar productos existentes
        List<Product> products = productRepository.findAllByDeletedFalse();
        assertFalse(products.isEmpty(), "Debe existir al menos un producto para el test");
        Product product = products.get(0);

        // 2. Agregar producto al carrito del usuario cliente
        String username = "client@gmail.com";
        SaleOrder initialCart = saleOrderService.getOrCreateCart(username);
        for (OrderDetail d : saleOrderService.getOrderDetails(initialCart.getId())) {
            saleOrderService.removeCartItem(username, d.getId());
        }
        SaleOrder cart = saleOrderService.addProductToCart(username, product.getId(), 2);

        assertNotNull(cart, "El carrito no debe ser nulo");
        assertEquals(OrderStatus.ON_CART, cart.getStatus(), "El estado inicial debe ser ON_CART");
        assertTrue(cart.getTotalAmount().compareTo(BigDecimal.ZERO) > 0, "El total debe ser mayor a 0");

        List<OrderDetail> details = saleOrderService.getOrderDetails(cart.getId());
        assertFalse(details.isEmpty(), "El carrito debe tener detalles");
        assertEquals(2, details.get(0).getQuantity(), "La cantidad debe ser 2");

        // 3. Procesar Checkout con Efectivo
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        assertFalse(offices.isEmpty(), "Debe existir al menos una sucursal");
        Office office = offices.get(0);

        String resultUrl = saleOrderService.processCheckout(
                username,
                office.getId(),
                "Av. San Martin",
                "1250",
                "Piso 2",
                "Depto B",
                "M5500",
                "Mendoza",
                "+54 9 261 455-1234",
                PaymentMethod.CASH
        );

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/checkout/success"));

        // La orden debe estar ahora en PENDING_PAYMENT
        SaleOrder order = saleOrderService.getOrderById(cart.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());

        // 4. Empleado actualiza estado a PAGO REALIZADO (PAID)
        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
        order = saleOrderService.getOrderById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, order.getStatus());

        // 5. Empleado actualiza estado a PENDIENTE DE ENVIO y luego ENTREGADO
        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.PENDING_SHIPPING);
        order = saleOrderService.getOrderById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING_SHIPPING, order.getStatus());

        saleOrderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        order = saleOrderService.getOrderById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    @Transactional
    public void testMercadoPagoCallbackFlow() {
        List<Product> products = productRepository.findAllByDeletedFalse();
        assertFalse(products.isEmpty());
        Product product = products.get(0);

        String username = "client@gmail.com";
        SaleOrder cart = saleOrderService.addProductToCart(username, product.getId(), 1);

        List<Office> offices = officeRepository.findAllByDeletedFalse();
        Office office = offices.get(0);

        // Checkout con Mercado Pago
        String redirectUrl = saleOrderService.processCheckout(
                username,
                office.getId(),
                "Av. San Martin",
                "1250",
                null,
                null,
                "M5500",
                "Mendoza",
                "+54 9 261 455-1234",
                PaymentMethod.MERCADO_PAGO
        );

        assertNotNull(redirectUrl);

        // Simular retorno exitoso de Mercado Pago
        SaleOrder paidOrder = saleOrderService.handleMercadoPagoSuccess(cart.getId());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
    }
}
