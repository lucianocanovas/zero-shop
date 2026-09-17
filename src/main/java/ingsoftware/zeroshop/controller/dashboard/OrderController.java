package ingsoftware.zeroshop.controller.dashboard;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardOrderController")
public class OrderController {

    // GET /dashboard/sale-orders: Lista todas las órdenes de los clientes
    @GetMapping("/dashboard/sale-orders")
    public String listOrders() {
        // LOGICA PARA LISTAR ORDENES
        return "dashboard/sale-orders";
    }

    // GET /dashboard/sale-orders/:id: Muestra el detalle de una orden de cliente
    @GetMapping("/dashboard/sale-orders/{id}")
    public String getOrderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE ORDEN
        return "dashboard/order-detail";
    }

    // POST /dashboard/sale-orders/: Crea una orden manualmente desde el panel
    @PostMapping("/dashboard/sale-orders/")
    public String createOrder() {
        // LOGICA PARA CREAR ORDEN MANUAL
        return "redirect:/dashboard/sale-orders";
    }

    // PUT /dashboard/sale-orders/:id: Actualiza datos de una orden
    @PutMapping("/dashboard/sale-orders/{id}")
    public String updateOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR DATOS DE LA ORDEN
        return "redirect:/dashboard/sale-orders/" + id;
    }

    // PUT /dashboard/sale-orders/:id/status: Actualiza el estado de la orden (PAGO REALIZADO, ENVIADO, ENTREGADO, etc.)
    @PutMapping("/dashboard/sale-orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR ESTADO DE LA ORDEN
        return "redirect:/dashboard/sale-orders/" + id;
    }

    // DELETE /dashboard/sale-orders/:id: Elimina una orden de cliente
    @DeleteMapping("/dashboard/sale-orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN
        return "redirect:/dashboard/sale-orders";
    }

}

