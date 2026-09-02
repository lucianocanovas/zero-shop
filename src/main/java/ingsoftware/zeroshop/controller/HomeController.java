package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Controlador para manejar la vista de inicio y redirecciones según el rol del usuario
@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Método para manejar la vista de inicio
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        // Si el usuario está autenticado, obtener su nombre y verificar si es administrador
        if (loggedIn) {
            String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(user -> user.getFirst_name())
                    .orElse(authentication.getName());
            model.addAttribute("userName", firstName);
            boolean isAdmin = hasRole(authentication, "ADMIN");
            model.addAttribute("isAdmin", isAdmin);

            // Redirigir al panel de administración si el usuario es administrador
            // if (isAdmin) {
            //     return "redirect:/admin/dashboard";
            // }
        }

        // Devolver la vista de inicio para usuarios no autenticados o clientes
        return "index";
    }

    // Método para manejar la vista del cliente
    @GetMapping("/client")
    public String clientView(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        // Si el usuario está autenticado, obtener su nombre y verificar si es administrador
        if (loggedIn) {
            String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(user -> user.getFirst_name())
                    .orElse(authentication.getName());
            model.addAttribute("userName", firstName);
            model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        }

        // Devolver la vista del cliente
        return "index";
    }

    // Metodo para manejar la vista del panel de administración
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        // Obtener el nombre del usuario autenticado y agregar atributos al modelo para la vista
        String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> user.getFirst_name())
                .orElse(authentication.getName());
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", firstName);
        return "admin/dashboard";
    }

    // Método para manejar la vista del perfil del usuario
    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        // Verificar si el usuario está autenticado
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        // Redirigir al panel de administración si el usuario tiene el rol de administrador, de lo contrario redirigir a la vista del cliente
        if (hasRole(authentication, "ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        // Redirigir a la vista del cliente si el usuario no es administrador
        return "redirect:/client";
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