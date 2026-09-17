package ingsoftware.zeroshop.controller.dashboard.employee;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("dashboardEmployeeController")
public class EmployeeController {

    // GET /dashboard/employee: Panel principal o escritorio del empleado
    @GetMapping({"/dashboard/employee", "/dashboard/employee/"})
    public String employeeDesk(Model model) {
        model.addAttribute("title", "Escritorio del Empleado - Zero Shop");
        return "dashboard/employee/index";
    }

    // GET /dashboard/employee/desk: Operaciones rápidas asignadas al empleado
    @GetMapping("/dashboard/employee/desk")
    public String employeeDeskDetails(Model model) {
        model.addAttribute("title", "Mi Sucursal y Tareas - Zero Shop");
        return "dashboard/employee/desk";
    }

}

