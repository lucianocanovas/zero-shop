package ingsoftware.zeroshop.controller.dashboard.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("dashboardAdminReportController")
public class ReportController {

    // GET /dashboard/admin/reports: Muestra el menú principal de reportes
    @GetMapping("/dashboard/admin/reports")
    public String reportsMenu() {
        // LOGICA PARA EL MENU DE REPORTES
        return "dashboard/admin/reports";
    }

    // GET /dashboard/admin/reports/sales: Muestra el reporte de ventas con filtros de fecha
    @GetMapping("/dashboard/admin/reports/sales")
    public String salesReport() {
        // LOGICA PARA REPORTE DE VENTAS (FILTRO POR FECHAS)
        return "dashboard/admin/reports-sales";
    }

    // GET /dashboard/admin/reports/stock: Muestra el reporte de stock con semáforo y enlace a WhatsApp
    @GetMapping("/dashboard/admin/reports/stock")
    public String stockReport() {
        // LOGICA PARA REPORTE DE STOCK POR SUCURSAL
        return "dashboard/admin/reports-stock";
    }

    // GET /dashboard/admin/reports/suppliers: Muestra el reporte de proveedores con costos más económicos
    @GetMapping("/dashboard/admin/reports/suppliers")
    public String suppliersReport() {
        // LOGICA PARA REPORTE DE PROVEEDORES
        return "dashboard/admin/reports-suppliers";
    }

}

