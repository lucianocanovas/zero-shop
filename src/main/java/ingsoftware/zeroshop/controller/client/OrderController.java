package ingsoftware.zeroshop.controller.client;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller("clientOrderController")
public class OrderController {

    // GET /orders: Muestra el listado de compras realizadas por el cliente
    @GetMapping("/orders")
    public String getOrders() {
        // LOGICA PARA LISTAR ORDENES DEL CLIENTE
        return "client/orders";
    }

    // GET /orders/:id: Muestra el detalle y seguimiento de una compra
    @GetMapping("/orders/{id}")
    public String getOrderById(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE Y ESTADO DE SEGUIMIENTO DE LA ORDEN
        return "client/order-detail";
    }

    // DELETE /orders/:id: Elimina una compra
    @DeleteMapping("/orders/{id}")
    public String deleteOrder(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR ORDEN
        return "redirect:/orders";
    }

}
