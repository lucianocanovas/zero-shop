package ingsoftware.zeroshop.controller.dashboard;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardProviderController")
public class ProviderController {

    // GET /dashboard/providers: Lista los proveedores registrados
    @GetMapping("/dashboard/providers")
    public String listProviders() {
        // LOGICA PARA LISTAR PROVEEDORES
        return "dashboard/providers";
    }

    // GET /dashboard/providers/:id: Muestra el detalle o edición de un proveedor
    @GetMapping("/dashboard/providers/{id}")
    public String getProviderDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE PROVEEDOR
        return "dashboard/provider-detail";
    }

    // POST /dashboard/providers: Registra un nuevo proveedor
    @PostMapping("/dashboard/providers")
    public String createProvider() {
        // LOGICA PARA CREAR PROVEEDOR (RAZON SOCIAL, EMAIL, WHATSAPP)
        return "redirect:/dashboard/providers";
    }

    // PUT /dashboard/providers/:id: Actualiza los datos de un proveedor
    @PutMapping("/dashboard/providers/{id}")
    public String updateProvider(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR PROVEEDOR
        return "redirect:/dashboard/providers";
    }

    // DELETE /dashboard/providers/:id: Elimina un proveedor
    @DeleteMapping("/dashboard/providers/{id}")
    public String deleteProvider(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR PROVEEDOR
        return "redirect:/dashboard/providers";
    }

}

