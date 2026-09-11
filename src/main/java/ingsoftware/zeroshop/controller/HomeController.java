package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Controlador para manejar las vistas públicas y principales de la tienda
@Controller
public class HomeController {

    private final UserService userService;

    // Constructor para inyectar la dependencia del servicio de usuario
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    // Método para manejar la vista de inicio
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        // Si el usuario está autenticado, obtener su nombre y verificar si es administrador
        if (loggedIn) {
            model.addAttribute("userName", getUserName(authentication));
            model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        // Devolver la vista de inicio para usuarios no autenticados o clientes
        return "index";
    }

    // Método para manejar la vista de productos
    @GetMapping("/products")
    public String products(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            model.addAttribute("userName", getUserName(authentication));
            model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        return "products";
    }

    // Método para manejar la vista del cliente
    @GetMapping("/client")
    public String clientView(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        // Si el usuario está autenticado, obtener su nombre y verificar si es administrador
        if (loggedIn) {
            model.addAttribute("userName", getUserName(authentication));
            model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        // Devolver la vista del cliente
        return "index";
    }

    // Método para verificar si el usuario está autenticado
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // Método para obtener el primer nombre del usuario autenticado a través del servicio
    private String getUserName(Authentication authentication) {
        return userService.getUserFirstName(authentication.getName());
    }

    // Método para verificar si el usuario tiene un rol específico
    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }
}