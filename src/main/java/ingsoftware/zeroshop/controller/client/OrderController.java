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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @Transactional(readOnly = true)
    public String getOrders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) ingsoftware.zeroshop.enums.OrderStatus status,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        int pageNum = (page != null && page > 0) ? page : 1;

        List<SaleOrder> orders;
        try {
            orders = saleOrderService.getClientOrders(principal.getName());
        } catch (Exception e) {
            orders = new ArrayList<>();
        }

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> o.getId() != null && o.getId().toString().toLowerCase().contains(q))
                    .toList();
        }

        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
        }

        // Ordenar por fecha descendente
        orders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getDate() == null) return 1;
                    if (o2.getDate() == null) return -1;
                    return o2.getDate().compareTo(o1.getDate());
                })
                .toList();

        ingsoftware.zeroshop.dto.PageResult<SaleOrder> pageResult = ingsoftware.zeroshop.dto.PageResult.of(orders, pageNum, 5);

        model.addAttribute("orders", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ingsoftware.zeroshop.enums.OrderStatus.values());

        return "client/orders";
    }

    // GET /orders/:id: Muestra el detalle y seguimiento de una compra
    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    public String getOrderById(@PathVariable("id") UUID id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<SaleOrder> orderOpt = saleOrderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }
        SaleOrder order = orderOpt.get();

        model.addAttribute("order", order);
        model.addAttribute("details", saleOrderService.getOrderDetails(id));
        model.addAttribute("payments", paymentService.getPaymentsByOrder(id));

        paymentService.getInvoiceByOrderId(id).ifPresent(invoice -> {
            model.addAttribute("invoice", invoice);
            model.addAttribute("invoiceDetails", paymentService.getInvoiceDetails(invoice.getId()));
        });

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
