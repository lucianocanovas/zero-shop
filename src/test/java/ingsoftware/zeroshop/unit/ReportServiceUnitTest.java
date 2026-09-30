package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.dto.SalesReportDTO;
import ingsoftware.zeroshop.dto.StockReportDTO;
import ingsoftware.zeroshop.dto.SupplierReportDTO;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.actor.SupplierProduct;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.actor.SupplierProductRepository;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.PaymentRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import ingsoftware.zeroshop.service.report.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceUnitTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private SupplierProductRepository supplierProductRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Unit: getSalesReport calcula total facturado e ítems vendidos")
    public void testGetSalesReport() {
        UUID orderId = UUID.randomUUID();
        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);
        order.setDate(LocalDateTime.now());
        order.setTotalAmount(new BigDecimal("50000.00"));

        Product product = Product.builder().name("Campera Zero Wind").build();
        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(2);
        detail.setUnitPrice(new BigDecimal("25000.00"));
        detail.setTotal(new BigDecimal("50000.00"));

        Payment payment = new Payment();
        payment.setMethod(PaymentMethod.MERCADO_PAGO);

        when(saleOrderRepository.findByDateBetweenAndDeletedFalse(any(), any())).thenReturn(List.of(order));
        when(paymentRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(payment));
        when(orderDetailRepository.findByOrderIdAndDeletedFalse(orderId)).thenReturn(List.of(detail));

        SalesReportDTO report = reportService.getSalesReport(LocalDate.now().minusDays(5), LocalDate.now());

        assertNotNull(report);
        assertEquals(1, report.totalOrders());
        assertEquals(new BigDecimal("50000.00"), report.totalBilled());
        assertEquals(1, report.details().size());
        assertEquals("Campera Zero Wind", report.details().get(0).productName());
        assertEquals("Mercado Pago", report.details().get(0).paymentMethod());
    }

    @Test
    @DisplayName("Unit: getStockReport calcula semáforo y genera link a WhatsApp con mensaje preformateado")
    public void testGetStockReportTrafficLightAndWhatsApp() {
        UUID prodId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Product product = Product.builder().id(prodId).name("Calzas Deportivas Pro").code("CLZ-01").build();
        Office office = new Office();
        office.setId(officeId);
        office.setName("Sucursal Mendoza");

        // 3 unidades de 50 de capacidad = 6% (< 20% -> MALO)
        Stock stock = Stock.builder().product(product).office(office).quantity(3).build();

        Supplier supplier = new Supplier();
        supplier.setName("Textil Cuyo S.A.");
        supplier.setContact(List.of(ContactPhone.builder().phoneNumber("+54 9 261 555-1234").build()));

        SupplierProduct supProd = new SupplierProduct();
        supProd.setSupplier(supplier);
        supProd.setCostPrice(new BigDecimal("8000.00"));

        when(productRepository.findAllByDeletedFalse()).thenReturn(List.of(product));
        when(officeRepository.findAllByDeletedFalse()).thenReturn(List.of(office));
        when(supplierProductRepository.findByProductIdAndDeletedFalseOrderByCostPriceAsc(prodId))
                .thenReturn(List.of(supProd));
        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(prodId, officeId))
                .thenReturn(Optional.of(stock));

        StockReportDTO report = reportService.getStockReport();

        assertNotNull(report);
        assertEquals(3, report.totalStockUnits());
        assertEquals(1, report.details().size());

        StockReportDTO.StockItemDTO item = report.details().get(0);
        assertEquals("MALO", item.trafficLight());
        assertEquals(22, item.neededUnits(), "Para alcanzar el 50% (25 uds), se requieren 25 - 3 = 22 unidades");
        assertNotNull(item.whatsappUrl());
        assertTrue(item.whatsappUrl().contains("wa.me"));
        assertTrue(item.whatsappUrl().contains("22"));
    }

    @Test
    @DisplayName("Unit: getSuppliersReport selecciona el proveedor más económico")
    public void testGetSuppliersReportCheapestPrice() {
        UUID prodId = UUID.randomUUID();
        Product product = Product.builder().id(prodId).name("Zapatillas Zero Air").build();

        Supplier sup1 = new Supplier();
        sup1.setName("Proveedor Económico");
        SupplierProduct sp1 = new SupplierProduct();
        sp1.setSupplier(sup1);
        sp1.setCostPrice(new BigDecimal("15000.00"));

        Supplier sup2 = new Supplier();
        sup2.setName("Proveedor Caro");
        SupplierProduct sp2 = new SupplierProduct();
        sp2.setSupplier(sup2);
        sp2.setCostPrice(new BigDecimal("18500.00"));

        when(productRepository.findAllByDeletedFalse()).thenReturn(List.of(product));
        when(supplierProductRepository.findByProductIdAndDeletedFalseOrderByCostPriceAsc(prodId))
                .thenReturn(List.of(sp1, sp2));

        SupplierReportDTO report = reportService.getSuppliersReport();

        assertNotNull(report);
        assertEquals(1, report.comparisons().size());
        SupplierReportDTO.SupplierComparisonDTO comp = report.comparisons().get(0);
        assertEquals("Proveedor Económico", comp.bestSupplierName());
        assertEquals(new BigDecimal("15000.00"), comp.bestCostPrice());
        assertEquals("Proveedor Caro", comp.altSupplierName());
        assertEquals(new BigDecimal("18500.00"), comp.altCostPrice());
        assertEquals(new BigDecimal("3500.00"), comp.unitSavings());
    }
}
