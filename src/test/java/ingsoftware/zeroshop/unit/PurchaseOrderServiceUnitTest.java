package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.PurchaseOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.PurchaseOrderRepository;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.PurchaseOrderService;
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
public class PurchaseOrderServiceUnitTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @Test
    @DisplayName("Unit: createPurchaseOrder calcula el total y persiste la orden y detalle")
    public void testCreatePurchaseOrderSuccess() {
        UUID supplierId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Proveedor Deportivo");

        Office office = new Office();
        office.setId(officeId);
        office.setName("Depósito Central");

        Product product = Product.builder().id(productId).name("Pelota Fútbol Pro").build();

        when(supplierRepository.findActive(supplierId)).thenReturn(Optional.of(supplier));
        when(officeRepository.findActive(officeId)).thenReturn(Optional.of(office));
        when(productRepository.findActive(productId)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> {
            PurchaseOrder po = i.getArgument(0);
            po.setId(UUID.randomUUID());
            return po;
        });

        PurchaseOrder created = purchaseOrderService.createPurchaseOrder(
                supplierId, officeId, productId, 20, new BigDecimal("4500.00"), null
        );

        assertNotNull(created);
        assertEquals(new BigDecimal("90000.00"), created.getTotalAmount());
        assertEquals(OrderStatus.PENDING_DELIVERY, created.getStatus());
        verify(orderDetailRepository, times(1)).save(any(OrderDetail.class));
    }

    @Test
    @DisplayName("Unit: receivePurchaseOrder incrementa el stock en la sucursal y pasa estado a DELIVERED")
    public void testReceivePurchaseOrderIncrementsStock() {
        UUID orderId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);
        office.setName("Depósito Mendoza");

        Product product = Product.builder().id(prodId).name("Musculosa").build();

        PurchaseOrder order = PurchaseOrder.builder()
                .id(orderId)
                .office(office)
                .status(OrderStatus.PENDING_DELIVERY)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .product(product)
                .quantity(50)
                .build();

        when(purchaseOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(detail));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrder received = purchaseOrderService.receivePurchaseOrder(orderId);

        assertEquals(OrderStatus.DELIVERED, received.getStatus());
        verify(stockService, times(1)).incrementStock(prodId, officeId, 50);
        verify(purchaseOrderRepository, times(1)).save(received);
    }

    @Test
    @DisplayName("Unit: receivePurchaseOrder arroja excepción si la orden ya fue recibida")
    public void testReceiveAlreadyDeliveredOrderThrows() {
        UUID orderId = UUID.randomUUID();
        PurchaseOrder order = PurchaseOrder.builder().id(orderId).status(OrderStatus.DELIVERED).build();

        when(purchaseOrderRepository.findActive(orderId)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                purchaseOrderService.receivePurchaseOrder(orderId)
        );
        assertTrue(ex.getMessage().contains("ya fue recibida"));
        verify(stockService, never()).incrementStock(any(), any(), any());
    }
}
