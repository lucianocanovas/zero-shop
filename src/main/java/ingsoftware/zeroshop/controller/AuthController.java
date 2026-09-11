package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    // Constructor para inyectar la dependencia del servicio de usuario y autenticación
    public AuthController(UserService userService, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    // GET /login: Muestra la página de inicio de sesión
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // POST /login: Maneja el inicio de sesión del usuario
    @PostMapping("/login")
    public String login() {
        // LOGICA DE INICIO DE SESION
        return "redirect:/";
    }

    // GET /register: Muestra la página de registro de usuario
    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    // POST /register: Maneja el registro de un nuevo usuario
    @PostMapping("/register")
    public String register() {
        // LOGICA DE REGISTRO DE USUARIO
        return "redirect:/verify";
    }

    // GET /verify: Muestra la página de verificación de correo electrónico
    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    // POST /verify: Maneja la verificación del correo electrónico del usuario
    @PostMapping("/verify")
    public String verify() {
        // LOGICA DE VERIFICACION DE CORREO ELECTRONICO
        return "redirect:/login";
    }

    // POST /verify/resend: Maneja el reenvío del correo de verificación
    @PostMapping("/verify/resend")
    public String resendVerificationEmail() {
        // LOGICA DE REENVIO DE CORREO DE VERIFICACION
        return "redirect:/verify";
    }

    // GET /logout: Maneja el cierre de sesión del usuario
    @GetMapping("/logout")
    public String logout() {
        // LOGICA DE CIERRE DE SESION
        return "redirect:/";
    }

}
