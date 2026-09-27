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

    // GET /dashboard/purchase-orders: Lista todas las órdenes de compra a proveedores
    @GetMapping("/dashboard/purchase-orders")
    public String listPurchaseOrders(Model model) {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        List<Supplier> suppliers = supplierRepository.findAllByDeletedFalse();
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        List<Product> products = productRepository.findAllByDeletedFalse();

        model.addAttribute("orders", orders);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("offices", offices);
        model.addAttribute("products", products);

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
