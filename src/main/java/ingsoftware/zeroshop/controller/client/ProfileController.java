package ingsoftware.zeroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class ProfileController {

    // GET /profile: Muestra el perfil del cliente
    @GetMapping("/profile")
    public String profilePage() {
        // LOGICA PARA OBTENER DATOS DEL PERFIL DEL CLIENTE
        return "client/profile";
    }

    // PUT /profile: Actualiza la información personal del cliente
    @PutMapping("/profile")
    public String updateProfile() {
        // LOGICA PARA ACTUALIZAR DATOS DEL CLIENTE
        return "redirect:/profile";
    }

}
