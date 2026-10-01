package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.dto.SupplierFormDTO;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.service.actor.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller("dashboardProviderController")
public class ProviderController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ingsoftware.zeroshop.repository.location.CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    // GET /dashboard/providers: Lista los proveedores registrados con búsqueda, filtros y paginación
    @GetMapping("/dashboard/providers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String listProviders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "hasContact", required = false) Boolean hasContact,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
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

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            providerDTOs = providerDTOs.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q))
                            || (p.getCuit() != null && p.getCuit().toLowerCase().contains(q))
                            || (p.getEmail() != null && p.getEmail().toLowerCase().contains(q))
                            || (p.getPhone() != null && p.getPhone().toLowerCase().contains(q)))
                    .toList();
        }

        if (hasContact != null) {
            providerDTOs = providerDTOs.stream()
                    .filter(p -> hasContact
                            ? ((p.getEmail() != null && !p.getEmail().isBlank()) || (p.getPhone() != null && !p.getPhone().isBlank()))
                            : ((p.getEmail() == null || p.getEmail().isBlank()) && (p.getPhone() == null || p.getPhone().isBlank())))
                    .toList();
        }

        ingsoftware.zeroshop.dto.PageResult<SupplierFormDTO> pageResult = ingsoftware.zeroshop.dto.PageResult.of(providerDTOs, pageNum, 10);

        model.addAttribute("providers", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("hasContact", hasContact);

        return "dashboard/providers";
    }

    // GET /dashboard/providers/new: Muestra el formulario para crear un nuevo proveedor
    @GetMapping("/dashboard/providers/new")
    public String newProviderForm(Model model) {
        model.addAttribute("supplierDTO", new SupplierFormDTO());
        model.addAttribute("countries", countryRepository.findAll());
        return "dashboard/provider-new";
    }

    // GET /dashboard/providers/:id: Muestra la vista para editar un proveedor existente
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

        java.util.List<Address> addresses = new java.util.ArrayList<>();
        if (supplier.getAddress() != null) {
            addresses = supplier.getAddress().stream()
                    .filter(a -> a.getDeleted() == null || !a.getDeleted())
                    .toList();
        }

        java.util.List<ingsoftware.zeroshop.entity.actor.Contact> contacts = new java.util.ArrayList<>();
        if (supplier.getContact() != null) {
            contacts = supplier.getContact().stream()
                    .filter(c -> !c.isDeleted())
                    .toList();
        }

        model.addAttribute("supplierDTO", dto);
        model.addAttribute("supplier", supplier);
        model.addAttribute("providerId", supplier.getId());
        model.addAttribute("addresses", addresses);
        model.addAttribute("contacts", contacts);
        model.addAttribute("countries", countryRepository.findAll());
        model.addAttribute("contactTypes", ingsoftware.zeroshop.enums.ContactType.values());
        model.addAttribute("phoneTypes", ingsoftware.zeroshop.enums.PhoneType.values());

        return "dashboard/provider-edit";
    }

    // POST /dashboard/providers: Registra un nuevo proveedor
    @PostMapping("/dashboard/providers")
    public String createProvider(@Valid @ModelAttribute SupplierFormDTO supplierDTO) {
        Supplier created = supplierService.createSupplier(supplierDTO);
        return "redirect:/dashboard/providers/" + created.getId();
    }

    // PUT /dashboard/providers/:id: Actualiza los datos comerciales de un proveedor
    @PutMapping("/dashboard/providers/{id}")
    public String updateProvider(@PathVariable("id") UUID id, @Valid @ModelAttribute SupplierFormDTO supplierDTO,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        supplierService.updateSupplier(id, supplierDTO);
        redirectAttributes.addFlashAttribute("success", "Datos del proveedor actualizados con éxito.");
        return "redirect:/dashboard/providers/" + id;
    }

    // DELETE /dashboard/providers/:id: Elimina un proveedor
    @DeleteMapping("/dashboard/providers/{id}")
    public String deleteProvider(@PathVariable("id") UUID id) {
        supplierService.deleteSupplier(id);
        return "redirect:/dashboard/providers";
    }

    // POST /dashboard/providers/:id/addresses: Agrega una dirección al proveedor
    @PostMapping("/dashboard/providers/{id}/addresses")
    public String addProviderAddress(@PathVariable("id") UUID id,
                                     @RequestParam("street") String street,
                                     @RequestParam("number") String number,
                                     @RequestParam(value = "floor", required = false) String floor,
                                     @RequestParam(value = "apartment", required = false) String apartment,
                                     @RequestParam(value = "zipCode", required = false) String zipCode,
                                     @RequestParam(value = "observations", required = false) String observations,
                                     @RequestParam(value = "cityId", required = false) UUID cityId,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (street == null || street.trim().isBlank()) throw new IllegalArgumentException("La calle es requerida.");
            if (number == null || number.trim().isBlank()) throw new IllegalArgumentException("El número de calle es requerido.");

            City city = (cityId != null) ? cityRepository.findById(cityId).orElse(null) : null;
            if (city == null) {
                city = cityRepository.findAllByDeletedFalse().stream().findFirst().orElse(null);
            }

            Address address = Address.builder()
                    .street(street.trim())
                    .number(number.trim())
                    .floor(floor != null && !floor.trim().isBlank() ? floor.trim() : null)
                    .apartment(apartment != null && !apartment.trim().isBlank() ? apartment.trim() : null)
                    .zipCode(zipCode != null && !zipCode.trim().isBlank() ? zipCode.trim() : "5500")
                    .observations(observations != null && !observations.trim().isBlank() ? observations.trim() : null)
                    .city(city)
                    .deleted(false)
                    .build();

            supplierService.addAddressToSupplier(id, address);
            redirectAttributes.addFlashAttribute("success", "Dirección agregada al proveedor con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar dirección: " + e.getMessage());
        }
        return "redirect:/dashboard/providers/" + id;
    }

    // POST /dashboard/providers/:id/addresses/:addressId/delete: Elimina una dirección del proveedor
    @PostMapping("/dashboard/providers/{id}/addresses/{addressId}/delete")
    public String deleteProviderAddress(@PathVariable("id") UUID id,
                                        @PathVariable("addressId") UUID addressId,
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        supplierService.removeAddressFromSupplier(id, addressId);
        redirectAttributes.addFlashAttribute("success", "Dirección eliminada del proveedor.");
        return "redirect:/dashboard/providers/" + id;
    }

    // POST /dashboard/providers/:id/contacts: Agrega un contacto al proveedor
    @PostMapping("/dashboard/providers/{id}/contacts")
    public String addProviderContact(@PathVariable("id") UUID id,
                                     @RequestParam("contactCategory") String contactCategory,
                                     @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                     @RequestParam(value = "phoneType", required = false) ingsoftware.zeroshop.enums.PhoneType phoneType,
                                     @RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value = "contactType", required = false) ingsoftware.zeroshop.enums.ContactType contactType,
                                     @RequestParam(value = "observation", required = false) String observation,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            ingsoftware.zeroshop.enums.ContactType cType = contactType != null ? contactType : ingsoftware.zeroshop.enums.ContactType.WORK;
            String obs = (observation != null && !observation.trim().isBlank()) ? observation.trim() : null;

            if ("EMAIL".equalsIgnoreCase(contactCategory)) {
                if (email == null || email.trim().isBlank()) throw new IllegalArgumentException("El correo electrónico es requerido.");
                ContactEmail ce = new ContactEmail();
                ce.setEmail(email.trim());
                ce.setContactType(cType);
                ce.setObservation(obs != null ? obs : "Email de contacto comercial");
                ce.setDeleted(false);
                supplierService.addContactToSupplier(id, ce);
            } else {
                if (phoneNumber == null || phoneNumber.trim().isBlank()) throw new IllegalArgumentException("El número de teléfono es requerido.");
                ContactPhone cp = new ContactPhone();
                cp.setPhoneNumber(phoneNumber.trim());
                cp.setPhoneType(phoneType != null ? phoneType : ingsoftware.zeroshop.enums.PhoneType.LANDLINE);
                cp.setContactType(cType);
                cp.setObservation(obs != null ? obs : "Teléfono comercial");
                cp.setDeleted(false);
                supplierService.addContactToSupplier(id, cp);
            }
            redirectAttributes.addFlashAttribute("success", "Contacto agregado al proveedor con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar contacto: " + e.getMessage());
        }
        return "redirect:/dashboard/providers/" + id;
    }

    // POST /dashboard/providers/:id/contacts/:contactId/delete: Elimina un contacto del proveedor
    @PostMapping("/dashboard/providers/{id}/contacts/{contactId}/delete")
    public String deleteProviderContact(@PathVariable("id") UUID id,
                                        @PathVariable("contactId") UUID contactId,
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        supplierService.removeContactFromSupplier(id, contactId);
        redirectAttributes.addFlashAttribute("success", "Contacto eliminado del proveedor.");
        return "redirect:/dashboard/providers/" + id;
    }
}
