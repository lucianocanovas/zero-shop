package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("adminUserController")
public class UserController {

    // GET /admin/users: Lista todos los usuarios del sistema
    @GetMapping("/admin/users")
    public String listUsers() {
        // LOGICA PARA LISTAR USUARIOS
        return "admin/users";
    }

    // GET /admin/users/:id: Muestra el detalle o edición de un usuario
    @GetMapping("/admin/users/{id}")
    public String getUserDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE USUARIO
        return "admin/user-detail";
    }

    // POST /admin/users: Guarda un nuevo usuario
    @PostMapping("/admin/users")
    public String createUser() {
        // LOGICA PARA CREAR USUARIO
        return "redirect:/admin/users";
    }

    // PUT /admin/users/:id: Actualiza los datos o rol de un usuario
    @PutMapping("/admin/users/{id}")
    public String updateUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR USUARIO
        return "redirect:/admin/users";
    }

    // DELETE /admin/users/:id: Da de baja un usuario
    @DeleteMapping("/admin/users/{id}")
    public String deleteUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR O DESACTIVAR USUARIO
        return "redirect:/admin/users";
    }

}
