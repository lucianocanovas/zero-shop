package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.PurchaseOrder;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.transaction.PurchaseOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller("dashboardPurchaseOrderController")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierRepository supplierRepository;
    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                   SupplierRepository supplierRepository,
                                   OfficeRepository officeRepository,
                                   ProductRepository productRepository) {
        this.purchaseOrderService = purchaseOrderService;
        this.supplierRepository = supplierRepository;
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
    }

    // GET /dashboard/purchase-orders: Lista todas las órdenes de compra a proveedores con búsqueda, filtros y paginación
    @GetMapping("/dashboard/purchase-orders")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String listPurchaseOrders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "supplierId", required = false) UUID supplierId,
            @RequestParam(name = "officeId", required = false) UUID officeId,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        List<Supplier> suppliers = supplierRepository.findAllByDeletedFalse();
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        List<Product> products = productRepository.findAllByDeletedFalse();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> (o.getId() != null && o.getId().toString().toLowerCase().contains(q))
                            || (o.getSupplier() != null && o.getSupplier().getName() != null && o.getSupplier().getName().toLowerCase().contains(q)))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        if (supplierId != null) {
            orders = orders.stream()
                    .filter(o -> o.getSupplier() != null && supplierId.equals(o.getSupplier().getId()))
                    .toList();
        }

        if (officeId != null) {
            orders = orders.stream()
                    .filter(o -> o.getOffice() != null && officeId.equals(o.getOffice().getId()))
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

        ingsoftware.zeroshop.dto.PageResult<PurchaseOrder> pageResult = ingsoftware.zeroshop.dto.PageResult.of(orders, pageNum, 10);

        model.addAttribute("orders", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("offices", offices);
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("officeId", officeId);

        return "dashboard/purchase-orders";
    }

    // GET /dashboard/purchase-orders/new: Muestra el formulario para emitir una nueva orden de compra
    @GetMapping("/dashboard/purchase-orders/new")
    public String newPurchaseOrderForm(Model model) {
        List<Supplier> suppliers = supplierRepository.findAllByDeletedFalse();
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        List<Product> products = productRepository.findAllByDeletedFalse();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("offices", offices);
        model.addAttribute("products", products);

        return "dashboard/purchase-order-new";
    }

    // GET /dashboard/purchase-orders/:id: Muestra el detalle de una orden de compra
    @GetMapping("/dashboard/purchase-orders/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String getPurchaseOrderDetail(@PathVariable("id") UUID id, Model model) {
        PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada con ID: " + id));
        List<OrderDetail> details = purchaseOrderService.getOrderDetails(id);

        model.addAttribute("order", order);
        model.addAttribute("details", details);

        return "dashboard/purchase-order-detail";
    }

    // POST /dashboard/purchase-orders: Genera una nueva orden de compra
    @PostMapping("/dashboard/purchase-orders")
    public String createPurchaseOrder(@RequestParam(value = "supplierId", required = false) UUID supplierId,
                                      @RequestParam(value = "providerId", required = false) UUID providerId,
                                      @RequestParam("officeId") UUID officeId,
                                      @RequestParam("productId") UUID productId,
                                      @RequestParam("quantity") Integer quantity,
                                      @RequestParam("unitPrice") BigDecimal unitPrice,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            UUID resolvedSupplierId = (supplierId != null) ? supplierId : providerId;
            String username = (principal != null) ? principal.getName() : null;

            PurchaseOrder order = purchaseOrderService.createPurchaseOrder(
                    resolvedSupplierId, officeId, productId, quantity, unitPrice, username
            );
            redirectAttributes.addFlashAttribute("successMessage", "Orden de compra generada exitosamente.");
            return "redirect:/dashboard/purchase-orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/purchase-orders";
        }
    }

    // POST /dashboard/purchase-orders/:id/receive: Marca la orden como recibida e incrementa automáticamente el stock
    @PostMapping("/dashboard/purchase-orders/{id}/receive")
    public String receivePurchaseOrder(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.receivePurchaseOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "¡Mercadería recibida exitosamente! El stock ha sido ingresado al inventario.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/purchase-orders/" + id;
    }

    // POST /dashboard/purchase-orders/:id/cancel: Cancela o elimina una orden de compra pendiente
    @PostMapping("/dashboard/purchase-orders/{id}/cancel")
    public String cancelPurchaseOrder(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.cancelPurchaseOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Orden de compra cancelada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard/purchase-orders";
    }

    // DELETE /dashboard/purchase-orders/:id: Para compatibilidad con peticiones DELETE
    @DeleteMapping("/dashboard/purchase-orders/{id}")
    public String deletePurchaseOrder(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        return cancelPurchaseOrder(id, redirectAttributes);
    }

}
