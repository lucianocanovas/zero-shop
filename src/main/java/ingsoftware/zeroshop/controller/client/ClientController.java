package ingsoftware.zeroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    // GET /: Muestra la página de inicio de la tienda
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Inicio - Tienda");
        return "client/index";
    }

    // GET /contact: Muestra la página de contacto e información institucional
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contacto");
        return "client/contact";
    }

}
