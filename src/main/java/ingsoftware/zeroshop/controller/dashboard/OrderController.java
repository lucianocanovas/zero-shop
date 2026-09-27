package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.service.transaction.PaymentService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller("dashboardOrderController")
public class OrderController {

    private final SaleOrderService saleOrderService;
    private final PaymentService paymentService;

    public OrderController(SaleOrderService saleOrderService, PaymentService paymentService) {
        this.saleOrderService = saleOrderService;
        this.paymentService = paymentService;
    }

    // GET /dashboard/sale-orders: Lista todas las órdenes de los clientes
    @GetMapping("/dashboard/sale-orders")
    public String listOrders(Model model) {
        List<SaleOrder> orders = saleOrderService.getAllConfirmedOrders();
        model.addAttribute("orders", orders);
        return "dashboard/sale-orders";
    }

    // GET /dashboard/sale-orders/:id: Muestra el detalle de una orden de cliente
    @GetMapping("/dashboard/sale-orders/{id}")
    public String getOrderDetail(@PathVariable("id") UUID id, Model model) {
        SaleOrder order = saleOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

        model.addAttribute("order", order);
        model.addAttribute("details", saleOrderService.getOrderDetails(id));
        model.addAttribute("payments", paymentService.getPaymentsByOrder(id));
        model.addAttribute("statuses", OrderStatus.values());

        return "dashboard/order-detail";
    }

    // PUT /dashboard/sale-orders/:id/status: Actualiza el estado de la orden
    @PutMapping("/dashboard/sale-orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") UUID id,
                                    @RequestParam("status") OrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            saleOrderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Estado de la orden actualizado a: " + status.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar estado: " + e.getMessage());
        }
        return "redirect:/dashboard/sale-orders/" + id;
    }

    // POST /dashboard/sale-orders/:id/status: Soporte para formularios directos
    @PostMapping("/dashboard/sale-orders/{id}/status")
    public String updateOrderStatusPost(@PathVariable("id") UUID id,
                                        @RequestParam("status") OrderStatus status,
                                        RedirectAttributes redirectAttributes) {
        return updateOrderStatus(id, status, redirectAttributes);
    }

    // DELETE /dashboard/sale-orders/:id: Elimina/anula una orden de cliente
    @DeleteMapping("/dashboard/sale-orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            saleOrderService.cancelOrder(id, "admin");
            redirectAttributes.addFlashAttribute("successMessage", "Orden cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cancelar la orden: " + e.getMessage());
        }
        return "redirect:/dashboard/sale-orders";
    }

}
