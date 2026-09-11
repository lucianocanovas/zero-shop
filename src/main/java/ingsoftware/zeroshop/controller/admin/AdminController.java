package ingsoftware.zeroshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // GET /admin/: Muestra el panel principal de administración
    @GetMapping({"/admin", "/admin/"})
    public String dashboard(Model model) {
        model.addAttribute("title", "Panel de Administración - Inicio");
        return "admin/index";
    }

}
