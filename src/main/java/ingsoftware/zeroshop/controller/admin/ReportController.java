package ingsoftware.zeroshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    // GET /admin/reports: Muestra el menú principal de reportes
    @GetMapping("/admin/reports")
    public String reportsMenu() {
        // LOGICA PARA EL MENU DE REPORTES
        return "admin/reports";
    }

    // GET /admin/reports/sales: Muestra el reporte de ventas con filtros de fecha
    @GetMapping("/admin/reports/sales")
    public String salesReport() {
        // LOGICA PARA REPORTE DE VENTAS (FILTRO POR FECHAS)
        return "admin/reports-sales";
    }

    // GET /admin/reports/stock: Muestra el reporte de stock con semáforo y enlace a WhatsApp
    @GetMapping("/admin/reports/stock")
    public String stockReport() {
        // LOGICA PARA REPORTE DE STOCK POR SUCURSAL
        return "admin/reports-stock";
    }

    // GET /admin/reports/suppliers: Muestra el reporte de proveedores con costos más económicos
    @GetMapping("/admin/reports/suppliers")
    public String suppliersReport() {
        // LOGICA PARA REPORTE DE PROVEEDORES
        return "admin/reports-suppliers";
    }

}
