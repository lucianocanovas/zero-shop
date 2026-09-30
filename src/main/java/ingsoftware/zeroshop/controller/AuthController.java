package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.service.actor.UserService;
import org.springframework.format.annotation.DateTimeFormat;
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

    // Constructor para inyectar la dependencia del servicio de usuario
    public AuthController(UserService userService) {
        this.userService = userService;
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
            return "redirect:/verify?email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) + "&sent=true";
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
    public String verifyPage(@RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "sent", required = false) Boolean sent,
                             Model model) {
        model.addAttribute("email", email);
        if (Boolean.TRUE.equals(sent)) {
            model.addAttribute("info", "Te hemos enviado un código de activación de 6 dígitos a tu correo. Ingrésalo a continuación para activar tu cuenta.");
        }
        return "verify";
    }

    // POST /verify: Maneja la verificación del correo electrónico del usuario
    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email,
                         @RequestParam("code") String code,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyAccount(email, code);
        if (verified) {
            redirectAttributes.addFlashAttribute("success", "¡Cuenta activada con éxito! Ya puedes iniciar sesión con tus credenciales.");
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("error", "Código de activación incorrecto o inexistente. Verifica e intenta nuevamente.");
            model.addAttribute("email", email);
            return "verify";
        }
    }

    // POST /verify/resend: Maneja el reenvío del correo de verificación
    @PostMapping("/verify/resend")
    public String resendVerificationEmail(@RequestParam("email") String email,
                                          RedirectAttributes redirectAttributes) {
        try {
            userService.resendVerificationCode(email);
            redirectAttributes.addFlashAttribute("info", "Se ha reenviado un nuevo código de activación a tu correo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo reenviar el código: " + e.getMessage());
        }
        return "redirect:/verify?email=" + (email != null ? java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) : "");
    }

    // GET /logout: Maneja el cierre de sesión del usuario
    @GetMapping("/logout")
    public String logout() {
        // LÓGICA DE CIERRE DE SESIÓN
        return "redirect:/";
    }

}
