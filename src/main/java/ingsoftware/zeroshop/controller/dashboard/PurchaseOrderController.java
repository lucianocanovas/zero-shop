package ingsoftware.zeroshop.controller.dashboard;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardPurchaseOrderController")
public class PurchaseOrderController {

    // GET /dashboard/purchase-orders: Lista todas las órdenes de compra a proveedores
    @GetMapping("/dashboard/purchase-orders")
    public String listPurchaseOrders() {
        // LOGICA PARA LISTAR ORDENES DE COMPRA
        return "dashboard/purchase-orders";
    }

    // GET /dashboard/purchase-orders/new: Muestra el formulario para emitir una nueva orden de compra
    @GetMapping("/dashboard/purchase-orders/new")
    public String newPurchaseOrderForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE ORDEN DE COMPRA
        return "dashboard/purchase-order-new";
    }

    // GET /dashboard/purchase-orders/:id: Muestra el detalle de una orden de compra
    @GetMapping("/dashboard/purchase-orders/{id}")
    public String getPurchaseOrderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE ORDEN DE COMPRA
        return "dashboard/purchase-order-detail";
    }

    // POST /dashboard/purchase-orders: Genera una nueva orden de compra
    @PostMapping("/dashboard/purchase-orders")
    public String createPurchaseOrder() {
        // LOGICA PARA CREAR ORDEN DE COMPRA
        return "redirect:/dashboard/purchase-orders";
    }

    // PUT /dashboard/purchase-orders/:id: Modifica una orden de compra pendiente
    @PutMapping("/dashboard/purchase-orders/{id}")
    public String updatePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR ORDEN DE COMPRA
        return "redirect:/dashboard/purchase-orders";
    }

    // POST /dashboard/purchase-orders/:id/receive: Marca la orden como recibida e incrementa automáticamente el stock
    @PostMapping("/dashboard/purchase-orders/{id}/receive")
    public String receivePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA MARCAR COMO ENTREGADA Y ACTUALIZAR STOCK
        return "redirect:/dashboard/purchase-orders/" + id;
    }

    // DELETE /dashboard/purchase-orders/:id: Cancela o elimina una orden de compra
    @DeleteMapping("/dashboard/purchase-orders/{id}")
    public String deletePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN DE COMPRA
        return "redirect:/dashboard/purchase-orders";
    }

}

