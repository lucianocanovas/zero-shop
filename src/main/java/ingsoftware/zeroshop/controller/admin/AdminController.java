package ingsoftware.zeroshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // GET /admin/: Muestra el panel principal de administración
    @GetMapping({"/admin", "/admin/"})
    public String dashboard() {
        // LOGICA DEL DASHBOARD DE ADMINISTRACION
        return "admin/index";
    }

}
