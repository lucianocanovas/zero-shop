package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.service.actor.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    // Constructor para inyectar la dependencia del servicio de usuario y autenticación
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // GET /login: Muestra la página de inicio de sesión
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // GET /register: Muestra la página de registro de usuario
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("idTypes", IDType.values());
        return "register";
    }

    // POST /register: Maneja el registro de un nuevo usuario cliente y su persona
    @PostMapping("/register")
    public String register(@RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName,
                           @RequestParam(value = "idType", required = false, defaultValue = "DNI") IDType idType,
                           @RequestParam(value = "idNumber", required = false) String idNumber,
                           @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        model.addAttribute("idTypes", IDType.values());
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("selectedIdType", idType);
        model.addAttribute("idNumber", idNumber);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("email", email);

        if (password == null || !password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas ingresadas no coinciden.");
            return "register";
        }

        try {
            userService.registerClient(firstName, lastName, idType, idNumber, dateOfBirth, email, password);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error inesperado al registrar la cuenta. Por favor verifica los datos e intenta nuevamente.");
            return "register";
        }
    }

    // GET /verify: Muestra la página de verificación de correo electrónico
    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    // POST /verify: Maneja la verificación del correo electrónico del usuario
    @PostMapping("/verify")
    public String verify() {
        // LÓGICA DE VERIFICACIÓN DE CORREO ELECTRÓNICO
        return "redirect:/login";
    }

    // POST /verify/resend: Maneja el reenvío del correo de verificación
    @PostMapping("/verify/resend")
    public String resendVerificationEmail() {
        // LÓGICA DE REENVÍO DE CORREO DE VERIFICACIÓN
        return "redirect:/verify";
    }

    // GET /logout: Maneja el cierre de sesión del usuario
    @GetMapping("/logout")
    public String logout() {
        // LÓGICA DE CIERRE DE SESIÓN
        return "redirect:/";
    }

}
