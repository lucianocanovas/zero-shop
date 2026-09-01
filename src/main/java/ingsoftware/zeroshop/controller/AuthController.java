package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controlador para manejar la autenticación y el registro de usuarios
@Controller
public class AuthController {

	private final UserService userService;

	// Constructor para inyectar la dependencia del servicio de usuario
	public AuthController(UserService userService) {
		this.userService = userService;
	}

	// Método para manejar la vista de inicio de sesión
	@GetMapping("/login")
	public String login() {
		return "login";
	}

	// Método para manejar la vista de registro
	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("registration", new RegistrationForm());
		return "register";
	}

	// Método para manejar el registro de un nuevo usuario
	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("registration") RegistrationForm form,
						   BindingResult bindingResult,
						   RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "register";
		}
		try {
			userService.register(form.getFirstName(), form.getLastName(), form.getEmail(), form.getPassword());
		} catch (IllegalArgumentException exception) {
			bindingResult.rejectValue("email", "duplicate", exception.getMessage());
			return "register";
		}
		redirectAttributes.addFlashAttribute("success", "Cuenta creada. Ya puedes iniciar sesion.");
		return "redirect:/login";
	}

	// Método para manejar la vista de cierre de sesión
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/login?logout";
	}

	// Clase interna para representar el formulario de registro de usuario
	@Data
	public static class RegistrationForm {
		@jakarta.validation.constraints.NotBlank
		private String firstName;
		@jakarta.validation.constraints.NotBlank
		private String lastName;
		@jakarta.validation.constraints.Email
		@jakarta.validation.constraints.NotBlank
		private String email;
		@jakarta.validation.constraints.NotBlank
		@jakarta.validation.constraints.Size(min = 8)
		private String password;
	}
}
