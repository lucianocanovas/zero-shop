package ingsoftware.zeroshop.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // GET /dashboard/: Muestra el panel principal de control (común para ADMIN y EMPLOYEE)
    @GetMapping({"/dashboard", "/dashboard/"})
    public String dashboard(Model model) {
        model.addAttribute("title", "Panel de Control - Dashboard");
        return "dashboard/index";
    }

}

