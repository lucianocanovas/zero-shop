package ingsoftware.zeroshop.controller.dashboard.admin;

import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.service.actor.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ingsoftware.zeroshop.dto.PageResult;
import ingsoftware.zeroshop.entity.actor.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller("dashboardAdminUserController")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /dashboard/users o /dashboard/admin/users: Lista todos los usuarios del sistema
    @GetMapping({"/dashboard/users", "/dashboard/admin/users"})
    @Transactional(readOnly = true)
    public String listUsers(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<User> users = userService.findAll();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            users = users.stream().filter(u -> {
                boolean matchUsername = u.getUsername() != null && u.getUsername().toLowerCase().contains(q);
                boolean matchPerson = false;
                if (u.getPerson() != null) {
                    String fullName = (u.getPerson().getFirstName() + " " + u.getPerson().getLastName()).toLowerCase();
                    boolean matchName = fullName.contains(q);
                    boolean matchDni = u.getPerson().getIdNumber() != null && u.getPerson().getIdNumber().toLowerCase().contains(q);
                    matchPerson = matchName || matchDni;
                }
                return matchUsername || matchPerson;
            }).toList();
        }

        if (role != null) {
            users = users.stream().filter(u -> u.getRole() == role).toList();
        }

        if (status != null && !status.isBlank()) {
            if ("active".equalsIgnoreCase(status)) {
                users = users.stream().filter(u -> Boolean.FALSE.equals(u.getDeleted())).toList();
            } else if ("inactive".equalsIgnoreCase(status)) {
                users = users.stream().filter(u -> Boolean.TRUE.equals(u.getDeleted())).toList();
            }
        }

        PageResult<User> pageResult = PageResult.of(users, pageNum, 10);

        model.addAttribute("users", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", Role.values());
        model.addAttribute("status", status);

        return "dashboard/admin/users";
    }

    // GET /dashboard/users/new o /dashboard/admin/users/new: Muestra el formulario para crear un nuevo usuario
    @GetMapping({"/dashboard/users/new", "/dashboard/admin/users/new"})
    public String newUserForm(Model model) {
        model.addAttribute("idTypes", IDType.values());
        model.addAttribute("roles", Role.values());
        return "dashboard/admin/user-new";
    }

    // GET /dashboard/users/:id o /dashboard/admin/users/:id: Muestra la vista para editar un usuario existente
    @GetMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String getUserDetail(@PathVariable("id") UUID id, Model model, RedirectAttributes redirectAttributes) {
        return userService.findById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("idTypes", IDType.values());
            model.addAttribute("roles", Role.values());
            return "dashboard/admin/user-edit";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/dashboard/admin/users";
        });
    }

    // POST /dashboard/users o /dashboard/admin/users: Guarda un nuevo usuario y su persona
    @PostMapping({"/dashboard/users", "/dashboard/admin/users"})
    public String createUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam(value = "idType", required = false, defaultValue = "DNI") IDType idType,
                             @RequestParam(value = "idNumber", required = false) String idNumber,
                             @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                             @RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("role") Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(firstName, lastName, idType, idNumber, dateOfBirth, username, password, role);
            redirectAttributes.addFlashAttribute("success", "Usuario creado exitosamente.");
            return "redirect:/dashboard/admin/users";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al crear el usuario.");
        }

        // Mantener valores ingresados en caso de error
        redirectAttributes.addFlashAttribute("firstName", firstName);
        redirectAttributes.addFlashAttribute("lastName", lastName);
        redirectAttributes.addFlashAttribute("selectedIdType", idType);
        redirectAttributes.addFlashAttribute("idNumber", idNumber);
        redirectAttributes.addFlashAttribute("dateOfBirth", dateOfBirth);
        redirectAttributes.addFlashAttribute("username", username);
        redirectAttributes.addFlashAttribute("selectedRole", role);
        return "redirect:/dashboard/admin/users/new";
    }

    // PUT /dashboard/users/:id o /dashboard/admin/users/:id: Actualiza los datos o rol de un usuario
    @PutMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String updateUser(@PathVariable("id") UUID id,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("username") String username,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam("role") Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, firstName, lastName, username, password, role);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al actualizar el usuario.");
        }
        return "redirect:/dashboard/admin/users";
    }

    // DELETE /dashboard/users/:id o /dashboard/admin/users/:id: Da de baja un usuario
    @DeleteMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String deleteUser(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Usuario dado de baja exitosamente.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al dar de baja el usuario.");
        }
        return "redirect:/dashboard/admin/users";
    }

}
