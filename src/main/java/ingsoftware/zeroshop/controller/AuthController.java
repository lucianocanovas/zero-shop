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

@Controller
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("registration", new RegistrationForm());
		return "register";
	}

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

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/login?logout";
	}

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
