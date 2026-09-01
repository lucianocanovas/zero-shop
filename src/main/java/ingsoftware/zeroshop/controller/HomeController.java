package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(user -> user.getFirst_name())
                    .orElse(authentication.getName());
            model.addAttribute("userName", firstName);
            boolean isAdmin = hasRole(authentication, "ADMIN");
            model.addAttribute("isAdmin", isAdmin);

            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
        }

        return "index";
    }

    @GetMapping("/client")
    public String clientView(Authentication authentication, Model model) {
        boolean loggedIn = isAuthenticated(authentication);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(user -> user.getFirst_name())
                    .orElse(authentication.getName());
            model.addAttribute("userName", firstName);
            model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        }

        return "index";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        String firstName = userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> user.getFirst_name())
                .orElse(authentication.getName());
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("userName", firstName);
        return "admin/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        if (hasRole(authentication, "ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/client";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }
}