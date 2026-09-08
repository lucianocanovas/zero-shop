package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.UserRepository;
import ingsoftware.zeroshop.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

// Gestion de la vista de usuarios para el administrador
@Controller
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Método para manejar la vista de usuarios
    @GetMapping("/admin/users")
    public String users(Authentication authentication, Model model) {

        // Verificar si el usuario está autenticado y tiene el rol de administrador
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        // Agregar atributos al modelo para la vista
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", getUserName(authentication));
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/user.html")
    public String profile(Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }
        User user = currentUser(authentication);
        addProfileModel(model, user, false, "/user.html");
        return "user";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUser(@PathVariable UUID id, Authentication authentication, Model model) {
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/users";
        }
        addProfileModel(model, user, true, "/admin/users/" + id);
        return "user";
    }

    @PostMapping("/user.html")
    public String updateOwnProfile(Authentication authentication,
                                  @ModelAttribute ProfileForm form,
                                  RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }
        User currentUser = currentUser(authentication);
        try {
            User updated = userService.updateProfile(currentUser.getId(), form.getFirstName(), form.getLastName(),
                    form.getEmail(), form.getPassword());
            refreshAuthentication(authentication, updated);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/user.html";
    }

    @PostMapping("/admin/users/{id}")
    public String updateUser(@PathVariable UUID id, Authentication authentication,
                             @ModelAttribute ProfileForm form, RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null || target.getRole() == Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Solo se pueden modificar usuarios no administradores.");
            return "redirect:/admin/users";
        }
        try {
            userService.updateProfile(id, form.getFirstName(), form.getLastName(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }
        try {
            userService.deleteNonAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void addProfileModel(Model model, User user, boolean adminEdit, String formAction) {
        model.addAttribute("profileUser", user);
        model.addAttribute("adminEdit", adminEdit);
        model.addAttribute("formAction", formAction);
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        model.addAttribute("userName", user.getFirst_name());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe."));
    }

    private void refreshAuthentication(Authentication authentication, User user) {
        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), authentication.getCredentials(), authentication.getAuthorities());
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }


    // Método para obtener el nombre del usuario autenticado
    private String getUserName(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> user.getFirst_name())
                .orElse(authentication.getName());
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

    @lombok.Data
    public static class ProfileForm {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
    }
}
