package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.repository.UserRepository;
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

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    // Método para manejar la vista del panel de administración de usuarios
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

    // Metodo para obtener el nombre del usuario autenticado
    private String getUserName(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> user.getFirst_name())
                .orElse(authentication.getName());
    }

    // Metodo para verificar si el usuario está autenticado
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // Metodo para verificar si el usuario tiene un rol específico
    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }
}
