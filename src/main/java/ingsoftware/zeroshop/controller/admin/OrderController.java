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


    // GET /admin/sale-orders: Lista todas las órdenes de los clientes
    @GetMapping("/admin/sale-orders")
    public String listOrders() {
        // LOGICA PARA LISTAR ORDENES
        return "admin/sale-orders";
    }

    // GET /admin/sale-orders/:id: Muestra el detalle de una orden de cliente
    @GetMapping("/admin/sale-orders/{id}")
    public String getOrderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE ORDEN
        return "admin/order-detail";
    }


    // POST /admin/sale-orders/: Crea una orden manualmente desde administración
    @PostMapping("/admin/sale-orders/")
    public String createOrder() {
        // LOGICA PARA CREAR ORDEN MANUAL
        return "redirect:/admin/sale-orders";
    }

    // PUT /admin/sale-orders/:id: Actualiza datos de una orden
    @PutMapping("/admin/sale-orders/{id}")
    public String updateOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR DATOS DE LA ORDEN
        return "redirect:/admin/sale-orders/" + id;
    }

    // PUT /admin/sale-orders/:id/status: Actualiza el estado de la orden (PAGO REALIZADO, ENVIADO, ENTREGADO, etc.)
    @PutMapping("/admin/sale-orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR ESTADO DE LA ORDEN
        return "redirect:/admin/sale-orders/" + id;
    }

    // DELETE /admin/sale-orders/:id: Elimina una orden de cliente
    @DeleteMapping("/admin/sale-orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN
        return "redirect:/admin/sale-orders";
    }

}
