package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.location.AddressRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import ingsoftware.zeroshop.service.notification.EmailService;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.MercadoPagoService;
import ingsoftware.zeroshop.service.transaction.PaymentService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
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
public class SaleOrderServiceUnitTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private StockService stockService;

    @Mock
    private MercadoPagoService mercadoPagoService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SaleOrderService saleOrderService;

    @Test
    @DisplayName("Unit: Transición de PENDING_PAYMENT a PAID descuenta stock y persiste cambio")
    public void testUpdateStatusToPaidDecrementsStock() {
        UUID orderId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);
        Product product = Product.builder().id(prodId).name("Remera Zero").build();

        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOffice(office);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(3);

        when(saleOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(detail));

        saleOrderService.updateOrderStatus(orderId, OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(saleOrderRepository, times(1)).save(order);
        verify(stockService, times(1)).decrementStock(prodId, officeId, 3);
    }

    @Test
    @DisplayName("Unit: Anulación de orden en estado DELIVERED arroja IllegalStateException")
    public void testCancelDeliveredOrderFails() {
        UUID orderId = UUID.randomUUID();
        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERED);

        when(saleOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                saleOrderService.cancelOrder(orderId, "cliente@test.com")
        );
        assertTrue(ex.getMessage().contains("No se puede cancelar una orden que ya ha sido entregada"));
        verify(saleOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Anulación de orden previamente pagada reabastece el stock del producto")
    public void testCancelPaidOrderRestoresStock() {
        UUID orderId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);
        Product product = Product.builder().id(prodId).name("Zapatillas Zero").build();

        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOffice(office);
        order.setStatus(OrderStatus.PAID);

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(2);

        when(saleOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(detail));

        saleOrderService.cancelOrder(orderId, "cliente@test.com");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(stockService, times(1)).incrementStock(prodId, officeId, 2);
        verify(saleOrderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Unit: handleMercadoPagoSuccess actualiza a PAID, descuenta stock y registra pago")
    public void testHandleMercadoPagoSuccess() {
        UUID orderId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);
        Product product = Product.builder().id(prodId).name("Campera").build();

        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setFirstName("Lucas");

        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOffice(office);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(new BigDecimal("45000.00"));

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(1);

        when(saleOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(detail));
        when(userRepository.findAllByDeletedFalse()).thenReturn(List.of());

        SaleOrder result = saleOrderService.handleMercadoPagoSuccess(orderId);

        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(paymentService, times(1)).registerPayment(order, new BigDecimal("45000.00"), PaymentMethod.MERCADO_PAGO);
        verify(stockService, times(1)).decrementStock(prodId, officeId, 1);
    }
}
