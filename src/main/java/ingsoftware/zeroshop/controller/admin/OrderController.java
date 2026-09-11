package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("adminOrderController")
public class OrderController {

    // GET /admin/orders: Lista todas las órdenes de los clientes
    @GetMapping("/admin/orders")
    public String listOrders() {
        // LOGICA PARA LISTAR ORDENES
        return "admin/orders";
    }

    // GET /admin/orders/:id: Muestra el detalle de una orden de cliente
    @GetMapping("/admin/orders/{id}")
    public String getOrderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE ORDEN
        return "admin/order-detail";
    }

    // POST /admin/orders/: Crea una orden manualmente desde administración
    @PostMapping("/admin/orders/")
    public String createOrder() {
        // LOGICA PARA CREAR ORDEN MANUAL
        return "redirect:/admin/orders";
    }

    // PUT /admin/orders/:id: Actualiza datos de una orden
    @PutMapping("/admin/orders/{id}")
    public String updateOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR DATOS DE LA ORDEN
        return "redirect:/admin/orders/" + id;
    }

    // PUT /admin/orders/:id/status: Actualiza el estado de la orden (PAGO REALIZADO, ENVIADO, ENTREGADO, etc.)
    @PutMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR ESTADO DE LA ORDEN
        return "redirect:/admin/orders/" + id;
    }

    // DELETE /admin/orders/:id: Elimina una orden de cliente
    @DeleteMapping("/admin/orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN
        return "redirect:/admin/orders";
    }

}
