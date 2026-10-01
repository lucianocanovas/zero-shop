package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.dto.ClientProfileDTO;
import ingsoftware.zeroshop.entity.actor.*;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.service.actor.UserService;
import ingsoftware.zeroshop.service.location.LocationService;
import ingsoftware.zeroshop.service.notification.NewsletterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final LocationService locationService;
    private final NewsletterService newsletterService;

    public ProfileController(UserService userService,
                             LocationService locationService,
                             NewsletterService newsletterService) {
        this.userService = userService;
        this.locationService = locationService;
        this.newsletterService = newsletterService;
    }

    // GET /profile: Muestra el perfil del cliente/usuario con sus direcciones y contactos
    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        User user = userService.getByEmail(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }

        ClientProfileDTO profile = new ClientProfileDTO();
        profile.setEmail(user.getUsername());
        profile.setEmailPromotionsEnabled(user.getEmailPromotionsEnabled() == null || user.getEmailPromotionsEnabled());

        Person person = user.getPerson();
        java.util.List<Address> addresses = new ArrayList<>();
        java.util.List<Contact> contacts = new ArrayList<>();

        if (person != null) {
            profile.setFirstName(person.getFirstName());
            profile.setLastName(person.getLastName());
            profile.setGender(person.getGender());
            profile.setDateOfBirth(person.getDateOfBirth());

            // Teléfono predeterminado si existe para el formulario base
            if (person.getContact() != null) {
                contacts = person.getContact().stream()
                        .filter(c -> !c.isDeleted())
                        .toList();

                for (Contact c : contacts) {
                    if (c instanceof ContactPhone cp && cp.getPhoneNumber() != null && !cp.getPhoneNumber().isBlank()) {
                        profile.setPhone(cp.getPhoneNumber());
                        break;
                    }
                }
            }

            // Direcciones activas
            if (person.getAddress() != null) {
                addresses = person.getAddress().stream()
                        .filter(a -> a.getDeleted() == null || !a.getDeleted())
                        .toList();

                if (!addresses.isEmpty()) {
                    Address first = addresses.get(0);
                    profile.setStreet(first.getStreet());
                    profile.setNumber(first.getNumber());
                    profile.setFloor(first.getFloor());
                    profile.setApartment(first.getApartment());
                    profile.setZipCode(first.getZipCode());
                    profile.setDepartment(first.getObservations() != null && !first.getObservations().isBlank()
                            ? first.getObservations() : "Capital");
                    if (first.getCity() != null) {
                        profile.setCity(first.getCity().getName());
                        if (first.getCity().getState() != null) {
                            profile.setState(first.getCity().getState().getName());
                        }
                    }
                }
            }
        } else {
            profile.setFirstName(user.getUsername() != null ? user.getUsername().split("@")[0] : "");
            profile.setLastName("");
        }

        // Valores por defecto para formulario
        if (profile.getState() == null || profile.getState().isBlank()) profile.setState("Mendoza");
        if (profile.getDepartment() == null || profile.getDepartment().isBlank()) profile.setDepartment("Capital");
        if (profile.getCity() == null || profile.getCity().isBlank()) profile.setCity("Mendoza");
        if (profile.getZipCode() == null || profile.getZipCode().isBlank()) profile.setZipCode("5500");

        model.addAttribute("profile", profile);
        model.addAttribute("addresses", addresses);
        model.addAttribute("contacts", contacts);
        model.addAttribute("countries", locationService.findAllCountries());
        model.addAttribute("contactTypes", ContactType.values());
        model.addAttribute("phoneTypes", PhoneType.values());
        return "client/profile";
    }

    // Actualiza los datos personales básicos y credenciales
    @RequestMapping(value = "/profile", method = {RequestMethod.POST, RequestMethod.PUT})
    public String updateProfile(Authentication authentication,
                                @ModelAttribute("profile") ClientProfileDTO form,
                                RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        try {
            User savedUser = userService.updateUserProfile(authentication.getName(), form);
            refreshAuthentication(authentication, savedUser);
            redirectAttributes.addFlashAttribute("success", "¡Datos personales actualizados correctamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al guardar el perfil: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // POST /profile/promotions/toggle: Activa o desactiva las promociones por email vía slider
    @PostMapping("/profile/promotions/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleEmailPromotions(
            @RequestParam(name = "enabled", required = false) Boolean enabled,
            Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }
        try {
            boolean newStatus = userService.toggleEmailPromotions(authentication.getName(), enabled);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "enabled", newStatus,
                    "message", newStatus ? "Promociones por email activadas correctamente." : "Promociones por email desactivadas."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }
    }

    // POST /profile/promotions/test-email: Envía un email promocional de prueba al usuario actual
    @PostMapping("/profile/promotions/test-email")
    public Object sendTestEmail(
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"));

        if (!isAuthenticated(authentication)) {
            if (isAjax) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Debes iniciar sesión para realizar esta acción."));
            }
            return "redirect:/login";
        }

        User user = userService.getByEmail(authentication.getName());
        if (user == null) {
            if (isAjax) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Usuario no encontrado."));
            }
            return "redirect:/login";
        }

        try {
            newsletterService.sendTestPromotionalEmail(user.getUsername());
            if (isAjax) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email promocional de prueba enviado exitosamente a: " + user.getUsername()
                ));
            }
            redirectAttributes.addFlashAttribute("success", "Email promocional de prueba enviado exitosamente a: " + user.getUsername());
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se pudo enviar el correo de prueba: " + e.getMessage()
                ));
            }
            redirectAttributes.addFlashAttribute("error", "No se pudo enviar el correo de prueba: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Agregar nueva dirección al perfil
    @PostMapping("/profile/addresses")
    public String addAddress(Authentication authentication,
                             @RequestParam("street") String street,
                             @RequestParam("number") String number,
                             @RequestParam(value = "floor", required = false) String floor,
                             @RequestParam(value = "apartment", required = false) String apartment,
                             @RequestParam(value = "zipCode", required = false) String zipCode,
                             @RequestParam(value = "observations", required = false) String observations,
                             @RequestParam(value = "cityId", required = false) UUID cityId,
                             @RequestParam(value = "cityName", required = false) String cityName,
                             RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) return "redirect:/login";

        try {
            City city = null;
            if (cityId != null) {
                city = locationService.findCityById(cityId).orElse(null);
            }
            if (city == null && cityName != null && !cityName.trim().isBlank()) {
                city = locationService.findCityByName(cityName.trim()).orElse(null);
            }
            if (city == null) {
                city = locationService.findFirstCity().orElse(null);
            }

            userService.addAddressToUserProfile(authentication.getName(), street, number, floor, apartment, zipCode, observations, city);
            redirectAttributes.addFlashAttribute("success", "¡Dirección agregada correctamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar dirección: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Eliminar dirección del perfil
    @PostMapping("/profile/addresses/{addressId}/delete")
    public String deleteAddress(Authentication authentication,
                                @PathVariable("addressId") UUID addressId,
                                RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) return "redirect:/login";
        try {
            userService.deleteAddressFromUserProfile(authentication.getName(), addressId);
            redirectAttributes.addFlashAttribute("success", "¡Dirección eliminada correctamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar dirección: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Agregar nuevo contacto al perfil
    @PostMapping("/profile/contacts")
    public String addContact(Authentication authentication,
                             @RequestParam("contactCategory") String contactCategory,
                             @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                             @RequestParam(value = "phoneType", required = false) PhoneType phoneType,
                             @RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "contactType", required = false) ContactType contactType,
                             @RequestParam(value = "observation", required = false) String observation,
                             RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) return "redirect:/login";

        try {
            userService.addContactToUserProfile(authentication.getName(), contactCategory, phoneNumber, phoneType, email, contactType, observation);
            redirectAttributes.addFlashAttribute("success", "¡Contacto agregado correctamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar contacto: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // Eliminar contacto del perfil
    @PostMapping("/profile/contacts/{contactId}/delete")
    public String deleteContact(Authentication authentication,
                                @PathVariable("contactId") UUID contactId,
                                RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(authentication)) return "redirect:/login";
        try {
            userService.deleteContactFromUserProfile(authentication.getName(), contactId);
            redirectAttributes.addFlashAttribute("success", "¡Contacto eliminado correctamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar contacto: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private void refreshAuthentication(Authentication authentication, User user) {
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authentication.getAuthorities()
        );
        UsernamePasswordAuthenticationToken updatedAuth =
                new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), authentication.getAuthorities());
        updatedAuth.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuth);
    }
}
