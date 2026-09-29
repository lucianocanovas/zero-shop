package ingsoftware.zeroshop.controller.dashboard.admin;

import java.util.UUID;
import ingsoftware.zeroshop.dto.OfficeFormDTO;
import ingsoftware.zeroshop.dto.AddressDTO;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.service.org.OfficeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import ingsoftware.zeroshop.repository.location.CountryRepository;

@Controller("dashboardAdminOfficeController")
public class OfficeController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private OfficeService officeService;

    // GET /dashboard/offices o /dashboard/admin/offices: Lista todas las sucursales
    @GetMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    public String listOffices(Model model) {
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
        model.addAttribute("offices", officeDTOs);
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
            addressDTO.setZipCode(entityAddress.getZipCode());
            addressDTO.setFloor(entityAddress.getFloor());
            addressDTO.setApartment(entityAddress.getApartment());
            addressDTO.setObservations(entityAddress.getObservations());
            if (entityAddress.getCity() != null) {
                addressDTO.setCityId(entityAddress.getCity().getId());
                if (entityAddress.getCity().getState() != null) {
                    addressDTO.setStateId(entityAddress.getCity().getState().getId());
                    if (entityAddress.getCity().getState().getCountry() != null) {
                        addressDTO.setCountryId(entityAddress.getCity().getState().getCountry().getId());
                    }
                }
            }
            dto.setAddress(addressDTO);
        }

        model.addAttribute("officeDTO", dto);
        model.addAttribute("officeId", office.getId());
        model.addAttribute("countries", countryRepository.findAll());
        return "dashboard/admin/office-edit";
    }

    // POST /dashboard/offices o /dashboard/admin/offices: Registra una nueva sucursal
    @PostMapping({"/dashboard/offices", "/dashboard/admin/offices"})
    public String createOffice(@Valid @ModelAttribute OfficeFormDTO officeDTO) {
        officeService.createOffice(officeDTO);
        return "redirect:/dashboard/admin/offices";
    }

    // PUT /dashboard/offices/:id o /dashboard/admin/offices/:id: Actualiza los datos de una sucursal
    @PutMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String updateOffice(@PathVariable("id") UUID id, @Valid @ModelAttribute OfficeFormDTO officeDTO) {
        officeService.updateOffice(id, officeDTO);
        return "redirect:/dashboard/admin/offices";
    }

    // DELETE /dashboard/offices/:id o /dashboard/admin/offices/:id: Elimina una sucursal
    @DeleteMapping({"/dashboard/offices/{id}", "/dashboard/admin/offices/{id}"})
    public String deleteOffice(@PathVariable("id") UUID id) {
        officeService.deleteOffice(id);
        return "redirect:/dashboard/admin/offices";
    }

}

