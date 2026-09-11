package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class ProviderController {

    // GET /admin/providers: Lista los proveedores registrados
    @GetMapping("/admin/providers")
    public String listProviders() {
        // LOGICA PARA LISTAR PROVEEDORES
        return "admin/providers";
    }

    // GET /admin/providers/:id: Muestra el detalle o edición de un proveedor
    @GetMapping("/admin/providers/{id}")
    public String getProviderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE PROVEEDOR
        return "admin/provider-detail";
    }

    // POST /admin/providers: Registra un nuevo proveedor
    @PostMapping("/admin/providers")
    public String createProvider() {
        // LOGICA PARA CREAR PROVEEDOR (RAZON SOCIAL, EMAIL, WHATSAPP)
        return "redirect:/admin/providers";
    }

    // PUT /admin/providers/:id: Actualiza los datos de un proveedor
    @PutMapping("/admin/providers/{id}")
    public String updateProvider(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR PROVEEDOR
        return "redirect:/admin/providers";
    }

    // DELETE /admin/providers/:id: Elimina un proveedor
    @DeleteMapping("/admin/providers/{id}")
    public String deleteProvider(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR PROVEEDOR
        return "redirect:/admin/providers";
    }

}
