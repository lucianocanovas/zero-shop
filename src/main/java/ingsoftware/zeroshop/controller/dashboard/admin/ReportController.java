package ingsoftware.zeroshop.controller.dashboard.admin;

import ingsoftware.zeroshop.dto.SalesReportDTO;
import ingsoftware.zeroshop.dto.StockReportDTO;
import ingsoftware.zeroshop.dto.SupplierReportDTO;
import ingsoftware.zeroshop.service.report.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller("dashboardAdminReportController")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // GET /dashboard/admin/reports: Muestra el menú principal de reportes
    @GetMapping("/dashboard/admin/reports")
    public String reportsMenu() {
        return "dashboard/admin/reports";
    }

    // GET /dashboard/admin/reports/sales: Muestra el reporte de ventas con filtros de fecha
    @GetMapping("/dashboard/admin/reports/sales")
    public String salesReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        SalesReportDTO report = reportService.getSalesReport(startDate, endDate);
        model.addAttribute("report", report);
        model.addAttribute("startDate", report.startDate());
        model.addAttribute("endDate", report.endDate());
        model.addAttribute("totalBilled", report.totalBilled());
        model.addAttribute("totalOrders", report.totalOrders());
        model.addAttribute("salesDetails", report.details());

        return "dashboard/admin/reports-sales";
    }

    // GET /dashboard/admin/reports/stock: Muestra el reporte de stock con semáforo y enlace a WhatsApp
    @GetMapping("/dashboard/admin/reports/stock")
    public String stockReport(Model model) {
        StockReportDTO report = reportService.getStockReport();
        model.addAttribute("report", report);
        model.addAttribute("totalStockUnits", report.totalStockUnits());
        model.addAttribute("pctGood", report.pctGood());
        model.addAttribute("pctRegular", report.pctRegular());
        model.addAttribute("pctBad", report.pctBad());
        model.addAttribute("stockDetails", report.details());

        return "dashboard/admin/reports-stock";
    }

    // GET /dashboard/admin/reports/suppliers: Muestra el reporte de proveedores con costos más económicos
    @GetMapping("/dashboard/admin/reports/suppliers")
    public String suppliersReport(Model model) {
        SupplierReportDTO report = reportService.getSuppliersReport();
        model.addAttribute("report", report);
        model.addAttribute("supplierComparisons", report.comparisons());

        return "dashboard/admin/reports-suppliers";
    }

}
