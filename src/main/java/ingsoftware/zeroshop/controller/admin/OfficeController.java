package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class OfficeController {

    // GET /admin/offices: Lista todas las sucursales
    @GetMapping("/admin/offices")
    public String listOffices() {
        // LOGICA PARA LISTAR SUCURSALES
        return "admin/offices";
    }

    // GET /admin/offices/new: Muestra el formulario para crear una nueva sucursal
    @GetMapping("/admin/offices/new")
    public String newOfficeForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE SUCURSAL
        return "admin/office-detail";
    }

    // GET /admin/offices/:id: Muestra el detalle o edición de una sucursal
    @GetMapping("/admin/offices/{id}")
    public String getOfficeDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE SUCURSAL
        return "admin/office-detail";
    }

    // POST /admin/offices: Registra una nueva sucursal
    @PostMapping("/admin/offices")
    public String createOffice() {
        // LOGICA PARA REGISTRAR SUCURSAL
        return "redirect:/admin/offices";
    }

    // PUT /admin/offices/:id: Actualiza los datos de una sucursal
    @PutMapping("/admin/offices/{id}")
    public String updateOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR SUCURSAL
        return "redirect:/admin/offices";
    }

    // DELETE /admin/offices/:id: Elimina una sucursal
    @DeleteMapping("/admin/offices/{id}")
    public String deleteOffice(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR SUCURSAL
        return "redirect:/admin/offices";
    }

}
