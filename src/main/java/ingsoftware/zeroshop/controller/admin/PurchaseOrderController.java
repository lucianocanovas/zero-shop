package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class PurchaseOrderController {

    // GET /admin/purchase-orders: Lista todas las órdenes de compra a proveedores
    @GetMapping("/admin/purchase-orders")
    public String listPurchaseOrders() {
        // LOGICA PARA LISTAR ORDENES DE COMPRA
        return "admin/purchase-orders";
    }

    // GET /admin/purchase-orders/:id: Muestra el detalle de una orden de compra
    @GetMapping("/admin/purchase-orders/{id}")
    public String getPurchaseOrderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE ORDEN DE COMPRA
        return "admin/purchase-order-detail";
    }

    // POST /admin/purchase-orders: Genera una nueva orden de compra
    @PostMapping("/admin/purchase-orders")
    public String createPurchaseOrder() {
        // LOGICA PARA CREAR ORDEN DE COMPRA
        return "redirect:/admin/purchase-orders";
    }

    // PUT /admin/purchase-orders/:id: Modifica una orden de compra pendiente
    @PutMapping("/admin/purchase-orders/{id}")
    public String updatePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR ORDEN DE COMPRA
        return "redirect:/admin/purchase-orders";
    }

    // POST /admin/purchase-orders/:id/receive: Marca la orden como recibida e incrementa automáticamente el stock
    @PostMapping("/admin/purchase-orders/{id}/receive")
    public String receivePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA MARCAR COMO ENTREGADA Y ACTUALIZAR STOCK
        return "redirect:/admin/purchase-orders/" + id;
    }

    // DELETE /admin/purchase-orders/:id: Cancela o elimina una orden de compra
    @DeleteMapping("/admin/purchase-orders/{id}")
    public String deletePurchaseOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN DE COMPRA
        return "redirect:/admin/purchase-orders";
    }

}
