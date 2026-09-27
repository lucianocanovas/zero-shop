package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.service.transaction.PaymentService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller("clientOrderController")
public class OrderController {

    private final SaleOrderService saleOrderService;
    private final PaymentService paymentService;

    public OrderController(SaleOrderService saleOrderService, PaymentService paymentService) {
        this.saleOrderService = saleOrderService;
        this.paymentService = paymentService;
    }

    // GET /orders: Muestra el listado de compras realizadas por el cliente
    @GetMapping("/orders")
    public String getOrders(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        List<SaleOrder> orders = saleOrderService.getClientOrders(principal.getName());
        model.addAttribute("orders", orders);
        return "client/orders";
    }

    // GET /orders/:id: Muestra el detalle y seguimiento de una compra
    @GetMapping("/orders/{id}")
    public String getOrderById(@PathVariable("id") UUID id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        SaleOrder order = saleOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

        model.addAttribute("order", order);
        model.addAttribute("details", saleOrderService.getOrderDetails(id));
        model.addAttribute("payments", paymentService.getPaymentsByOrder(id));

        return "client/order-detail";
    }

    // DELETE /orders/:id: Anula una compra
    @DeleteMapping("/orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            saleOrderService.cancelOrder(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Compra anulada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders";
    }

    // POST /orders/:id/cancel: Soporte para formularios sin DELETE HTTP
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrderPost(@PathVariable("id") UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        return deleteOrder(id, principal, redirectAttributes);
    }

}
