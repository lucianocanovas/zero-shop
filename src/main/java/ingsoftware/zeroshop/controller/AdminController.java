package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Controlador para manejar las vistas de administración
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    // Constructor para inyectar la dependencia del servicio de usuario
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Método para manejar la vista principal del panel de administración
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        return "admin/index";
    }

    // Método para manejar la vista del panel de administración de productos
    @GetMapping("/products")
    public String products(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        return "admin/products";
    }

    // Método para manejar la vista del panel de administración de pedidos
    @GetMapping("/orders")
    public String orders(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        return "admin/orders";
    }

    // Método para manejar la vista del panel de administración de stock
    @GetMapping("/stock")
    public String stock(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        return "admin/stock/index";
    }

    // Método para manejar la vista del panel de administración de destacados
    @GetMapping("/highlights")
    public String highlights(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        return "admin/highlights/index";
    }

    // Método para obtener el nombre del usuario autenticado a través del servicio
    private String getUserName(Authentication authentication) {
        return userService.getUserFirstName(authentication.getName());
    }

    // Método para verificar si el usuario está autenticado
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // Método para verificar si el usuario tiene un rol específico
    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }
}
