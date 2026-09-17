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

    // GET /dashboard/admin/offices: Lista todas las sucursales
    @GetMapping("/dashboard/admin/offices")
    public String listOffices() {
        // LOGICA PARA LISTAR SUCURSALES
        return "dashboard/admin/offices";
    }

    // GET /dashboard/admin/offices/new: Muestra el formulario para crear una nueva sucursal
    @GetMapping("/dashboard/admin/offices/new")
    public String newOfficeForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE SUCURSAL
        return "dashboard/admin/office-detail";
    }

    // GET /dashboard/admin/offices/:id: Muestra el detalle o edición de una sucursal
    @GetMapping("/dashboard/admin/offices/{id}")
    public String getOfficeDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE SUCURSAL
        return "dashboard/admin/office-detail";
    }

    // POST /dashboard/admin/offices: Registra una nueva sucursal
    @PostMapping("/dashboard/admin/offices")
    public String createOffice() {
        // LOGICA PARA REGISTRAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

    // PUT /dashboard/admin/offices/:id: Actualiza los datos de una sucursal
    @PutMapping("/dashboard/admin/offices/{id}")
    public String updateOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

    // DELETE /dashboard/admin/offices/:id: Elimina una sucursal
    @DeleteMapping("/dashboard/admin/offices/{id}")
    public String deleteOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR SUCURSAL
        return "redirect:/dashboard/admin/offices";
    }

}

