package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.Role;
import lombok.Data;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

// Controlador para gestionar las operaciones y vistas de usuarios y perfiles
@Controller
public class UserController {

    private final UserService userService;

    // Constructor para inyectar la dependencia del servicio de usuario
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Método para listar todos los usuarios registrados (vista de administración)
    @GetMapping({"/users", "/admin/users"})
    public String listUsers(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    // Método para mostrar el formulario de edición de un usuario específico por su ID
    @GetMapping({"/users/{id}", "/users/{id}/edit", "/admin/users/{id}/edit"})
    public String editUser(@PathVariable UUID id, Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        // Obtener el usuario mediante el servicio
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/users";
        }

        addProfileModel(model, user, true, "/users/" + id);
        return "user";
    }

    // Método para actualizar los datos de un usuario por su ID
    @PostMapping({"/users/{id}", "/admin/users/{id}"})
    public String updateUser(@PathVariable UUID id,
                             Authentication authentication,
                             @ModelAttribute ProfileForm form,
                             RedirectAttributes redirectAttributes) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        try {
            userService.updateNonAdminProfile(id, form.getFirstName(), form.getLastName(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users";
    }

    // Método para eliminar un usuario por su ID
    @PostMapping({"/users/{id}/delete", "/admin/users/{id}/delete"})
    public String deleteUser(@PathVariable UUID id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        try {
            userService.deleteNonAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users";
    }

    // Método para mostrar la vista del perfil propio del usuario autenticado
    @GetMapping({"/profile", "/user.html"})
    public String viewProfile(Authentication authentication, Model model) {
        // Verificar si el usuario está autenticado
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        User user = userService.getByEmail(authentication.getName());
        addProfileModel(model, user, false, "/profile");
        return "user";
    }

    // Método para actualizar el perfil propio del usuario autenticado
    @PostMapping({"/profile", "/user.html"})
    public String updateProfile(Authentication authentication,
                                @ModelAttribute ProfileForm form,
                                RedirectAttributes redirectAttributes) {
        // Verificar si el usuario está autenticado
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        User currentUser = userService.getByEmail(authentication.getName());
        try {
            User updated = userService.updateProfile(currentUser.getId(), form.getFirstName(), form.getLastName(),
                    form.getEmail(), form.getPassword());
            refreshAuthentication(authentication, updated);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/profile";
    }

    // Método auxiliar para preparar el modelo de la vista de perfil o edición de usuario
    private void addProfileModel(Model model, User user, boolean adminEdit, String formAction) {
        model.addAttribute("profileUser", user);
        model.addAttribute("adminEdit", adminEdit);
        model.addAttribute("formAction", formAction);
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        model.addAttribute("userName", user.getPersona() != null ? user.getPersona().getNombre() : user.getEmail());
    }

    // Método auxiliar para refrescar el contexto de seguridad tras actualizar datos del usuario
    private void refreshAuthentication(Authentication authentication, User user) {
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authentication.getAuthorities()
        );
        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), authentication.getAuthorities());
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }

    // Método para obtener el primer nombre del usuario autenticado
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

    // Formulario para representar los datos de edición de perfil y usuario
    @Data
    public static class ProfileForm {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
    }
}
