package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.service.catalog.ProductService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final SaleOrderService saleOrderService;
    private final ProductService productService;

    public DashboardController(SaleOrderService saleOrderService, ProductService productService) {
        this.saleOrderService = saleOrderService;
        this.productService = productService;
    }

    // GET /dashboard/: Muestra el panel principal de control (común para ADMIN y EMPLOYEE)
    @GetMapping({"/dashboard", "/dashboard/"})
    public String dashboard(Model model) {
        model.addAttribute("title", "Panel de Control - Dashboard");

        List<SaleOrder> allConfirmedOrders = saleOrderService.getAllConfirmedOrders();
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);

        BigDecimal salesThisMonth = allConfirmedOrders.stream()
                .filter(o -> o.getDate() != null && !o.getDate().toLocalDate().isBefore(startOfMonth))
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(o -> o.getTotalAmount())
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        long newOrdersCount = allConfirmedOrders.stream()
                .filter(o -> o.getDate() != null && !o.getDate().toLocalDate().isBefore(startOfMonth))
                .count();

        long pendingOrdersCount = allConfirmedOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT ||
                             o.getStatus() == OrderStatus.PAID ||
                             o.getStatus() == OrderStatus.PENDING_SHIPPING ||
                             o.getStatus() == OrderStatus.PENDING_DELIVERY)
                .count();

        List<Product> allActiveProducts = productService.findAllActive();
        long activeCatalogCount = allActiveProducts.size();

        long stockAlertsCount = allActiveProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .count();

        List<SaleOrder> recentOrders = allConfirmedOrders.stream()
                .limit(5)
                .toList();

        List<Product> criticalStockProducts = allActiveProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .sorted(Comparator.comparingInt(p -> p.getStock() != null ? p.getStock() : 0))
                .limit(5)
                .toList();

        model.addAttribute("salesThisMonth", salesThisMonth);
        model.addAttribute("newOrdersCount", newOrdersCount);
        model.addAttribute("pendingOrdersCount", pendingOrdersCount);
        model.addAttribute("activeCatalogCount", activeCatalogCount);
        model.addAttribute("stockAlertsCount", stockAlertsCount);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("criticalStockProducts", criticalStockProducts);

        return "dashboard/index";
    }

}
