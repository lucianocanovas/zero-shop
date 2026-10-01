package ingsoftware.zeroshop.controller.dashboard.admin;

import java.util.UUID;
import ingsoftware.zeroshop.dto.OfficeFormDTO;
import ingsoftware.zeroshop.dto.AddressDTO;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.service.org.OfficeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.location.CountryRepository;

@Controller("dashboardAdminOfficeController")
public class OfficeController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private OfficeService officeService;

    // GET /dashboard/offices o /dashboard/admin/offices: Lista todas las sucursales con búsqueda, filtro y paginación
    @GetMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    @Transactional(readOnly = true)
    public String listOffices(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "type", required = false) ingsoftware.zeroshop.enums.OfficeType type,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        java.util.List<OfficeFormDTO> officeDTOs = new java.util.ArrayList<>();
        for (Office office : officeService.getAllOffices()) {
            OfficeFormDTO dto = new OfficeFormDTO();
            dto.setId(office.getId());
            dto.setName(office.getName());
            dto.setCuit(office.getCuit());
            dto.setType(office.getType().name());
            
            if (office.getContact() != null) {
                office.getContact().forEach(c -> {
                    if (c instanceof ContactPhone)
                        dto.setPhone(((ContactPhone) c).getPhoneNumber());
                });
            }

            if (office.getAddress() != null && !office.getAddress().isEmpty()) {
                Address entityAddress = office.getAddress().iterator().next();
                AddressDTO addressDTO = new AddressDTO();
                addressDTO.setStreet(entityAddress.getStreet());
                addressDTO.setNumber(entityAddress.getNumber());
                dto.setAddress(addressDTO);
            }
            
            officeDTOs.add(dto);
        }

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            officeDTOs = officeDTOs.stream().filter(o -> {
                boolean matchName = o.getName() != null && o.getName().toLowerCase().contains(q);
                boolean matchCuit = o.getCuit() != null && o.getCuit().toLowerCase().contains(q);
                boolean matchPhone = o.getPhone() != null && o.getPhone().toLowerCase().contains(q);
                boolean matchAddress = false;
                if (o.getAddress() != null) {
                    String street = o.getAddress().getStreet() != null ? o.getAddress().getStreet().toLowerCase() : "";
                    String num = o.getAddress().getNumber() != null ? o.getAddress().getNumber().toString() : "";
                    matchAddress = street.contains(q) || num.contains(q);
                }
                return matchName || matchCuit || matchPhone || matchAddress;
            }).toList();
        }

        if (type != null) {
            officeDTOs = officeDTOs.stream().filter(o -> type.name().equalsIgnoreCase(o.getType())).toList();
        }

        ingsoftware.zeroshop.dto.PageResult<OfficeFormDTO> pageResult = ingsoftware.zeroshop.dto.PageResult.of(officeDTOs, pageNum, 10);

        model.addAttribute("offices", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("selectedType", type);
        model.addAttribute("officeTypes", ingsoftware.zeroshop.enums.OfficeType.values());

        return "dashboard/admin/offices";
    }

    // GET /dashboard/offices/new o /dashboard/admin/offices/new: Muestra el formulario para crear una nueva sucursal
    @GetMapping({"/dashboard/offices/new", "/dashboard/admin/offices/new"})
    public String newOfficeForm(Model model) {
        model.addAttribute("officeDTO", new OfficeFormDTO());
        model.addAttribute("countries", countryRepository.findAll());
        return "dashboard/admin/office-new";
    }

    // GET /dashboard/offices/:id o /dashboard/admin/offices/:id: Muestra la vista para editar una sucursal existente
    @GetMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String getOfficeDetail(@PathVariable("id") UUID id, Model model) {
        Office office = officeService.getOfficeById(id);
        if (office == null) return "redirect:/dashboard/admin/offices";

        OfficeFormDTO dto = new OfficeFormDTO();
        dto.setName(office.getName());
        dto.setCuit(office.getCuit());
        dto.setType(office.getType().name());

        java.util.List<Address> addresses = new java.util.ArrayList<>();
        if (office.getAddress() != null) {
            addresses = office.getAddress().stream()
                    .filter(a -> a.getDeleted() == null || !a.getDeleted())
                    .toList();
        }

        java.util.List<ingsoftware.zeroshop.entity.actor.Contact> contacts = new java.util.ArrayList<>();
        if (office.getContact() != null) {
            contacts = office.getContact().stream()
                    .filter(c -> !c.isDeleted())
                    .toList();
        }

        model.addAttribute("officeDTO", dto);
        model.addAttribute("officeId", office.getId());
        model.addAttribute("office", office);
        model.addAttribute("addresses", addresses);
        model.addAttribute("contacts", contacts);
        model.addAttribute("countries", countryRepository.findAll());
        model.addAttribute("officeTypes", ingsoftware.zeroshop.enums.OfficeType.values());
        model.addAttribute("contactTypes", ingsoftware.zeroshop.enums.ContactType.values());
        model.addAttribute("phoneTypes", ingsoftware.zeroshop.enums.PhoneType.values());
        return "dashboard/admin/office-edit";
    }

    // POST /dashboard/offices o /dashboard/admin/offices: Registra una nueva sucursal
    @PostMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    public String createOffice(@Valid @ModelAttribute OfficeFormDTO officeDTO) {
        Office created = officeService.createOffice(officeDTO);
        return "redirect:/dashboard/admin/offices/" + created.getId();
    }

    // PUT /dashboard/offices/:id o /dashboard/admin/offices/:id: Actualiza los datos de una sucursal
    @PutMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String updateOffice(@PathVariable("id") UUID id, @Valid @ModelAttribute OfficeFormDTO officeDTO,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        officeService.updateOffice(id, officeDTO);
        redirectAttributes.addFlashAttribute("success", "Datos de la sucursal actualizados correctamente.");
        return "redirect:/dashboard/admin/offices/" + id;
    }

    // DELETE /dashboard/offices/:id o /dashboard/admin/offices/:id: Elimina una sucursal
    @DeleteMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String deleteOffice(@PathVariable("id") UUID id) {
        officeService.deleteOffice(id);
        return "redirect:/dashboard/admin/offices";
    }

    // POST /dashboard/admin/offices/:id/addresses: Agrega una dirección a la sucursal
    @PostMapping({"/dashboard/offices/{id}/addresses", "/dashboard/admin/offices/{id}/addresses"})
    public String addOfficeAddress(@PathVariable("id") UUID id,
                                   @RequestParam("street") String street,
                                   @RequestParam("number") String number,
                                   @RequestParam(value = "floor", required = false) String floor,
                                   @RequestParam(value = "apartment", required = false) String apartment,
                                   @RequestParam(value = "zipCode", required = false) String zipCode,
                                   @RequestParam(value = "observations", required = false) String observations,
                                   @RequestParam(value = "cityId", required = false) UUID cityId,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (street == null || street.trim().isBlank()) throw new IllegalArgumentException("La calle es obligatoria.");
            if (number == null || number.trim().isBlank()) throw new IllegalArgumentException("El número es obligatorio.");

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

            officeService.addAddressToOffice(id, address);
            redirectAttributes.addFlashAttribute("success", "Dirección agregada a la sucursal con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar dirección: " + e.getMessage());
        }
        return "redirect:/dashboard/admin/offices/" + id;
    }

    // POST /dashboard/admin/offices/:id/addresses/:addressId/delete: Elimina una dirección de la sucursal
    @PostMapping({"/dashboard/offices/{id}/addresses/{addressId}/delete", "/dashboard/admin/offices/{id}/addresses/{addressId}/delete"})
    public String deleteOfficeAddress(@PathVariable("id") UUID id,
                                      @PathVariable("addressId") UUID addressId,
                                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        officeService.removeAddressFromOffice(id, addressId);
        redirectAttributes.addFlashAttribute("success", "Dirección eliminada de la sucursal.");
        return "redirect:/dashboard/admin/offices/" + id;
    }

    // POST /dashboard/admin/offices/:id/contacts: Agrega un contacto a la sucursal
    @PostMapping({"/dashboard/offices/{id}/contacts", "/dashboard/admin/offices/{id}/contacts"})
    public String addOfficeContact(@PathVariable("id") UUID id,
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
                ingsoftware.zeroshop.entity.actor.ContactEmail ce = new ingsoftware.zeroshop.entity.actor.ContactEmail();
                ce.setEmail(email.trim());
                ce.setContactType(cType);
                ce.setObservation(obs != null ? obs : "Email de la sucursal");
                ce.setDeleted(false);
                officeService.addContactToOffice(id, ce);
            } else {
                if (phoneNumber == null || phoneNumber.trim().isBlank()) throw new IllegalArgumentException("El número de teléfono es requerido.");
                ContactPhone cp = new ContactPhone();
                cp.setPhoneNumber(phoneNumber.trim());
                cp.setPhoneType(phoneType != null ? phoneType : ingsoftware.zeroshop.enums.PhoneType.LANDLINE);
                cp.setContactType(cType);
                cp.setObservation(obs != null ? obs : "Teléfono de la sucursal");
                cp.setDeleted(false);
                officeService.addContactToOffice(id, cp);
            }
            redirectAttributes.addFlashAttribute("success", "Canal de contacto agregado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar contacto: " + e.getMessage());
        }
        return "redirect:/dashboard/admin/offices/" + id;
    }

    // POST /dashboard/admin/offices/:id/contacts/:contactId/delete: Elimina un contacto de la sucursal
    @PostMapping({"/dashboard/offices/{id}/contacts/{contactId}/delete", "/dashboard/admin/offices/{id}/contacts/{contactId}/delete"})
    public String deleteOfficeContact(@PathVariable("id") UUID id,
                                      @PathVariable("contactId") UUID contactId,
                                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        officeService.removeContactFromOffice(id, contactId);
        redirectAttributes.addFlashAttribute("success", "Contacto eliminado de la sucursal.");
        return "redirect:/dashboard/admin/offices/" + id;
    }

}

