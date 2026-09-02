package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
	private final AuthenticationManager authenticationManager;

	// Constructor para inyectar la dependencia del servicio de usuario
	public AuthController(UserService userService, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
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
						   RedirectAttributes redirectAttributes,
						   HttpServletRequest request) {
		if (bindingResult.hasErrors()) {
			return "register";
		}
		// Validar que las contraseñas coincidan y no estén vacías
		String passwordConfirmation = form.getConfirmPassword();
		if (!passwordConfirmation.equals(form.getPassword()) || passwordConfirmation.isBlank()) {
			bindingResult.rejectValue("confirmPassword", "mismatch", "Las contraseñas no coinciden.");
			return "register";
		}
		try {
			userService.register(form.getFirstName(), form.getLastName(), form.getEmail(), form.getPassword(), passwordConfirmation);
			var authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword())
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			HttpSession session = request.getSession(true);
			session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext());
		} catch (IllegalArgumentException exception) {
			bindingResult.rejectValue("email", "duplicate", exception.getMessage());
			return "register";
		}
		redirectAttributes.addFlashAttribute("success", "Cuenta creada. Ya puedes iniciar sesion.");
		return "redirect:/";
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
		@jakarta.validation.constraints.NotBlank
		private String confirmPassword;
	}
}
