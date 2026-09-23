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

    // GET /dashboard/users o /dashboard/admin/users: Lista todos los usuarios del sistema
    @GetMapping({"/dashboard/users", "/dashboard/admin/users"})
    public String listUsers() {
        // LOGICA PARA LISTAR USUARIOS
        return "dashboard/admin/users";
    }

    // GET /dashboard/users/new o /dashboard/admin/users/new: Muestra el formulario para crear un nuevo usuario
    @GetMapping({"/dashboard/users/new", "/dashboard/admin/users/new"})
    public String newUserForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE USUARIO
        return "dashboard/admin/user-new";
    }

    // GET /dashboard/users/:id o /dashboard/admin/users/:id: Muestra la vista para editar un usuario existente
    @GetMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String getUserDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER EDICION DE USUARIO
        return "dashboard/admin/user-edit";
    }

    // POST /dashboard/users o /dashboard/admin/users: Guarda un nuevo usuario
    @PostMapping({"/dashboard/users", "/dashboard/admin/users"})
    public String createUser() {
        // LOGICA PARA CREAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

    // PUT /dashboard/users/:id o /dashboard/admin/users/:id: Actualiza los datos o rol de un usuario
    @PutMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String updateUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

    // DELETE /dashboard/users/:id o /dashboard/admin/users/:id: Da de baja un usuario
    @DeleteMapping({"/dashboard/users/{id}", "/dashboard/admin/users/{id}"})
    public String deleteUser(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR O DESACTIVAR USUARIO
        return "redirect:/dashboard/admin/users";
    }

}

