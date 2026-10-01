package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.transaction.Invoice;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.service.org.OfficeService;
import ingsoftware.zeroshop.service.transaction.PaymentService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller("dashboardOrderController")
public class OrderController {

    private final SaleOrderService saleOrderService;
    private final PaymentService paymentService;
    private final OfficeService officeService;

    public OrderController(SaleOrderService saleOrderService,
                           PaymentService paymentService,
                           OfficeService officeService) {
        this.saleOrderService = saleOrderService;
        this.paymentService = paymentService;
        this.officeService = officeService;
    }

    // GET /dashboard/sale-orders: Lista todas las órdenes de los clientes con búsqueda, filtros y paginación
    @GetMapping("/dashboard/sale-orders")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String listOrders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "officeId", required = false) UUID officeId,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<SaleOrder> orders = saleOrderService.getAllConfirmedOrders();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> (o.getId() != null && o.getId().toString().toLowerCase().contains(q))
                            || (o.getClient() != null && (
                                    (o.getClient().getFirstName() != null && o.getClient().getFirstName().toLowerCase().contains(q))
                                    || (o.getClient().getLastName() != null && o.getClient().getLastName().toLowerCase().contains(q))
                                    || (o.getClient().getIdNumber() != null && o.getClient().getIdNumber().toLowerCase().contains(q))
                            )))
                    .toList();
        }

        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
        }

        if (officeId != null) {
            orders = orders.stream()
                    .filter(o -> o.getOffice() != null && officeId.equals(o.getOffice().getId()))
                    .toList();
        }

        // Ordenar fecha descendente
        orders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getDate() == null) return 1;
                    if (o2.getDate() == null) return -1;
                    return o2.getDate().compareTo(o1.getDate());
                })
                .toList();

        ingsoftware.zeroshop.dto.PageResult<SaleOrder> pageResult = ingsoftware.zeroshop.dto.PageResult.of(orders, pageNum, 10);

        model.addAttribute("orders", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("offices", officeService.getAllOffices());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("officeId", officeId);

        return "dashboard/sale-orders";
    }

    // GET /dashboard/sale-orders/:id: Muestra el detalle de una orden de cliente
    @GetMapping("/dashboard/sale-orders/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String getOrderDetail(@PathVariable("id") UUID id, Model model) {
        SaleOrder order = saleOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

        model.addAttribute("order", order);
        model.addAttribute("details", saleOrderService.getOrderDetails(id));
        model.addAttribute("payments", paymentService.getPaymentsByOrder(id));
        model.addAttribute("statuses", OrderStatus.values());

        // Factura asociada
        Optional<Invoice> invoiceOpt = paymentService.getInvoiceByOrderId(id);
        if (invoiceOpt.isEmpty() && isOrderPaidOrCompleted(order.getStatus())) {
            Invoice autoInvoice = paymentService.createInvoiceForOrder(order);
            invoiceOpt = Optional.of(autoInvoice);
        }

        invoiceOpt.ifPresent(invoice -> {
            model.addAttribute("invoice", invoice);
            model.addAttribute("invoiceDetails", paymentService.getInvoiceDetails(invoice.getId()));
        });

        return "dashboard/order-detail";
    }

    private boolean isOrderPaidOrCompleted(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.PENDING_SHIPPING
                || status == OrderStatus.PENDING_DELIVERY
                || status == OrderStatus.DELIVERED;
    }

    // POST /dashboard/sale-orders/:id/generate-invoice: Emitir factura manualmente
    @PostMapping("/dashboard/sale-orders/{id}/generate-invoice")
    public String generateInvoice(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            SaleOrder order = saleOrderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
            Invoice invoice = paymentService.createInvoiceForOrder(order);
            redirectAttributes.addFlashAttribute("successMessage", "¡Factura " + invoice.getNumber() + " generada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al emitir factura: " + e.getMessage());
        }
        return "redirect:/dashboard/sale-orders/" + id;
    }

    // POST /dashboard/sale-orders/:id/pay-cash: Registrar cobro en efectivo y descontar stock
    @PostMapping("/dashboard/sale-orders/{id}/pay-cash")
    public String payOrderWithCash(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            saleOrderService.payOrderWithCash(id);
            redirectAttributes.addFlashAttribute("successMessage", "¡Cobro en efectivo registrado y factura emitida exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar cobro en efectivo: " + e.getMessage());
        }
        return "redirect:/dashboard/sale-orders/" + id;
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
