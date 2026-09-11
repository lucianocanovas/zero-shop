package ingsoftware.zeroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    // GET /: Muestra la página de inicio de la tienda
    @GetMapping("/")
    public String index() {
        // LOGICA PARA MOSTRAR LA PAGINA DE INICIO
        return "client/index";
    }

    // GET /contact: Muestra la página de contacto e información institucional
    @GetMapping("/contact")
    public String contact() {
        // LOGICA PARA MOSTRAR LA PAGINA DE CONTACTO
        return "client/contact";
    }

}
