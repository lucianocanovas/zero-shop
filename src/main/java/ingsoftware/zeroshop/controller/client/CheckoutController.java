package ingsoftware.zeroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckoutController {

    // GET /checkout: Muestra la página de finalización de compra
    @GetMapping("/checkout")
    public String checkoutPage() {
        // LOGICA PARA MOSTRAR RESUMEN Y METODOS DE PAGO
        return "client/checkout";
    }

    // POST /checkout: Procesa la compra y realiza el pago
    @PostMapping("/checkout")
    public String processCheckout() {
        // LOGICA PARA PROCESAR PAGO Y GENERAR ORDEN
        return "redirect:/checkout/success";
    }

    // GET /checkout/success: Muestra la confirmación de la compra realizada con éxito
    @GetMapping("/checkout/success")
    public String checkoutSuccess() {
        // LOGICA PARA MOSTRAR DETALLES DE COMPRA EXITOSA
        return "client/checkout-success";
    }

}
