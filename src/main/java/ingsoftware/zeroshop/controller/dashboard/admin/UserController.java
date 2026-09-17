package ingsoftware.zeroshop.controller.dashboard.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardAdminUserController")
public class UserController {

    // GET /dashboard/admin/users: Lista todos los usuarios del sistema
    @GetMapping("/dashboard/admin/users")
    public String listUsers() {
        // LOGICA PARA LISTAR USUARIOS
        return "dashboard/admin/users";
    }

    // GET /dashboard/admin/users/:id: Muestra el detalle o edición de un usuario
    @GetMapping("/dashboard/admin/users/{id}")
    public String getUserDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE USUARIO
        return "dashboard/admin/user-detail";
    }

    // POST /dashboard/admin/users: Guarda un nuevo usuario
    @PostMapping("/dashboard/admin/users")
    public String createUser() {
        // LOGICA PARA CREAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

    // PUT /dashboard/admin/users/:id: Actualiza los datos o rol de un usuario
    @PutMapping("/dashboard/admin/users/{id}")
    public String updateUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

    // DELETE /dashboard/admin/users/:id: Da de baja un usuario
    @DeleteMapping("/dashboard/admin/users/{id}")
    public String deleteUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR O DESACTIVAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

}

