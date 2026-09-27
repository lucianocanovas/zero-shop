package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.dto.SupplierFormDTO;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.service.actor.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller("dashboardProviderController")
public class ProviderController {

    @Autowired
    private SupplierService supplierService;

    // GET /dashboard/providers: Lista los proveedores registrados
    @GetMapping("/dashboard/providers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String listProviders(Model model) {
        java.util.List<SupplierFormDTO> providerDTOs = new java.util.ArrayList<>();
        for (Supplier supplier : supplierService.getAllSuppliers()) {
            SupplierFormDTO dto = new SupplierFormDTO();
            dto.setId(supplier.getId());
            dto.setName(supplier.getName());
            dto.setCuit(supplier.getCuit());
            if (supplier.getContact() != null) {
                supplier.getContact().forEach(c -> {
                    if (c instanceof ContactEmail)
                        dto.setEmail(((ContactEmail) c).getEmail());
                    if (c instanceof ContactPhone)
                        dto.setPhone(((ContactPhone) c).getPhoneNumber());
                });
            }
            providerDTOs.add(dto);
        }
        model.addAttribute("providers", providerDTOs);
        return "dashboard/providers";
    }

    // GET /dashboard/providers/new: Muestra el formulario para crear un nuevo
    // proveedor
    @GetMapping("/dashboard/providers/new")
    public String newProviderForm(Model model) {
        model.addAttribute("supplierDTO", new SupplierFormDTO());
        return "dashboard/provider-new";
    }

    // GET /dashboard/providers/:id: Muestra la vista para editar un proveedor
    // existente
    @GetMapping("/dashboard/providers/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String getProviderDetail(@PathVariable("id") UUID id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier == null) {
            return "redirect:/dashboard/providers";
        }

        SupplierFormDTO dto = new SupplierFormDTO();
        dto.setName(supplier.getName());
        dto.setCuit(supplier.getCuit());

        if (supplier.getContact() != null) {
            supplier.getContact().forEach(c -> {
                if (c instanceof ContactEmail)
                    dto.setEmail(((ContactEmail) c).getEmail());
                if (c instanceof ContactPhone)
                    dto.setPhone(((ContactPhone) c).getPhoneNumber());
            });
        }

        if (supplier.getAddress() != null && !supplier.getAddress().isEmpty()) {
            dto.setAddress(supplier.getAddress().iterator().next().getStreet());
        }

        model.addAttribute("supplierDTO", dto);
        model.addAttribute("providerId", supplier.getId());

        return "dashboard/provider-edit";
    }

    // POST /dashboard/providers: Registra un nuevo proveedor
    @PostMapping("/dashboard/providers")
    public String createProvider(@ModelAttribute SupplierFormDTO supplierDTO) {
        supplierService.createSupplier(supplierDTO);
        return "redirect:/dashboard/providers";
    }

    // PUT /dashboard/providers/:id: Actualiza los datos de un proveedor
    @PutMapping("/dashboard/providers/{id}")
    public String updateProvider(@PathVariable("id") UUID id, @ModelAttribute SupplierFormDTO supplierDTO) {
        supplierService.updateSupplier(id, supplierDTO);
        return "redirect:/dashboard/providers";
    }

    // DELETE /dashboard/providers/:id: Elimina un proveedor
    @DeleteMapping("/dashboard/providers/{id}")
    public String deleteProvider(@PathVariable("id") UUID id) {
        supplierService.deleteSupplier(id);
        return "redirect:/dashboard/providers";
    }

}
