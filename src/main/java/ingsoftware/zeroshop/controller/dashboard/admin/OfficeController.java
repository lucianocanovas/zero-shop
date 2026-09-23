package ingsoftware.zeroshop.controller.dashboard.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardAdminOfficeController")
public class OfficeController {

    // GET /dashboard/offices o /dashboard/admin/offices: Lista todas las sucursales
    @GetMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    public String listOffices() {
        // LOGICA PARA LISTAR SUCURSALES
        return "dashboard/admin/offices";
    }

    // GET /dashboard/offices/new o /dashboard/admin/offices/new: Muestra el formulario para crear una nueva sucursal
    @GetMapping({"/dashboard/offices/new", "/dashboard/admin/offices/new"})
    public String newOfficeForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE SUCURSAL
        return "dashboard/admin/office-new";
    }

    // GET /dashboard/offices/:id o /dashboard/admin/offices/:id: Muestra la vista para editar una sucursal existente
    @GetMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String getOfficeDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER EDICION DE SUCURSAL
        return "dashboard/admin/office-edit";
    }

    // POST /dashboard/offices o /dashboard/admin/offices: Registra una nueva sucursal
    @PostMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    public String createOffice() {
        // LOGICA PARA REGISTRAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

    // PUT /dashboard/offices/:id o /dashboard/admin/offices/:id: Actualiza los datos de una sucursal
    @PutMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String updateOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

    // DELETE /dashboard/offices/:id o /dashboard/admin/offices/:id: Elimina una sucursal
    @DeleteMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String deleteOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

}

