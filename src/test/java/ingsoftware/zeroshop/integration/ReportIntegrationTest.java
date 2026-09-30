package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.dto.SalesReportDTO;
import ingsoftware.zeroshop.dto.StockReportDTO;
import ingsoftware.zeroshop.dto.SupplierReportDTO;
import ingsoftware.zeroshop.service.report.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ReportIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Test
    @DisplayName("Integración: Reporte de Ventas por rango de fechas")
    public void testSalesReportIntegration() {
        LocalDate start = LocalDate.now().minusMonths(2);
        LocalDate end = LocalDate.now().plusDays(1);

        SalesReportDTO report = reportService.getSalesReport(start, end);

        assertNotNull(report);
        assertEquals(start, report.startDate());
        assertEquals(end, report.endDate());
        assertNotNull(report.totalBilled(), "El total facturado no debe ser nulo");
        assertTrue(report.totalBilled().compareTo(BigDecimal.ZERO) >= 0);
        assertNotNull(report.details());
    }

    @Test
    @DisplayName("Integración: Reporte de Stock con semáforo y enlaces a WhatsApp")
    public void testStockReportIntegration() {
        StockReportDTO report = reportService.getStockReport();

        assertNotNull(report);
        assertTrue(report.totalStockUnits() >= 0);
        assertNotNull(report.details());

        for (StockReportDTO.StockItemDTO item : report.details()) {
            assertNotNull(item.officeName());
            assertNotNull(item.productName());
            assertNotNull(item.trafficLight());
            assertTrue(item.trafficLight().matches("BIEN|REGULAR|MALO"));

            if ("MALO".equals(item.trafficLight())) {
                assertNotNull(item.whatsappUrl(), "Si el stock es malo debe generar link de WhatsApp");
                assertTrue(item.whatsappUrl().startsWith("https://wa.me/"));
                assertTrue(item.neededUnits() > 0, "Debe indicar cantidad requerida para llegar al 50%");
            }
        }
    }

    @Test
    @DisplayName("Integración: Reporte de Proveedores Económicos para reposición")
    public void testSuppliersReportIntegration() {
        SupplierReportDTO report = reportService.getSuppliersReport();

        assertNotNull(report);
        assertNotNull(report.comparisons());

        for (SupplierReportDTO.SupplierComparisonDTO comp : report.comparisons()) {
            assertNotNull(comp.productName());
            assertNotNull(comp.bestSupplierName());
            if (comp.bestCostPrice() != null && comp.altCostPrice() != null) {
                assertTrue(comp.bestCostPrice().compareTo(comp.altCostPrice()) <= 0,
                        "El proveedor principal debe tener un precio menor o igual al alternativo");
            }
        }
    }
}
