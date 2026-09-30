package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.dto.ClientProfileDTO;
import ingsoftware.zeroshop.entity.actor.*;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.EmployeeRepository;
import ingsoftware.zeroshop.repository.actor.PersonRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.service.actor.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final CityRepository cityRepository;
    private final ingsoftware.zeroshop.repository.location.CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService,
                             UserRepository userRepository,
                             PersonRepository personRepository,
                             ClientRepository clientRepository,
                             EmployeeRepository employeeRepository,
                             CityRepository cityRepository,
                             ingsoftware.zeroshop.repository.location.CountryRepository countryRepository,
                             PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
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
        model.addAttribute("countries", countryRepository.findAll());
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

        User user = userService.getByEmail(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }

        try {
            if (form.getFirstName() == null || form.getFirstName().trim().isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio.");
            }
            if (form.getLastName() == null || form.getLastName().trim().isBlank()) {
                throw new IllegalArgumentException("El apellido es obligatorio.");
            }
            if (form.getEmail() == null || form.getEmail().trim().isBlank()) {
                throw new IllegalArgumentException("El correo electrónico es obligatorio.");
            }

            String newEmail = form.getEmail().trim().toLowerCase();
            if (!newEmail.equals(user.getUsername().toLowerCase())) {
                Optional<User> other = userRepository.findByUsernameIgnoreCase(newEmail);
                if (other.isPresent() && !other.get().getId().equals(user.getId())
                        && (other.get().getDeleted() == null || !other.get().getDeleted())) {
                    throw new IllegalArgumentException("El correo electrónico ya está registrado por otro usuario.");
                }
                user.setUsername(newEmail);
            }

            if (form.getPassword() != null && !form.getPassword().isBlank()) {
                if (form.getPassword().trim().length() < 6) {
                    throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
                }
                user.setPassword(passwordEncoder.encode(form.getPassword().trim()));
            }

            Person person = getOrCreatePerson(user);
            person.setFirstName(form.getFirstName().trim());
            person.setLastName(form.getLastName().trim());
            person.setGender(form.getGender());
            if (form.getDateOfBirth() != null) {
                person.setDateOfBirth(form.getDateOfBirth());
            }

            personRepository.save(person);
            User savedUser = userRepository.save(user);
            refreshAuthentication(authentication, savedUser);

            redirectAttributes.addFlashAttribute("success", "¡Datos personales actualizados correctamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al guardar el perfil: " + e.getMessage());
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
        User user = userService.getByEmail(authentication.getName());
        if (user == null) return "redirect:/login";

        try {
            if (street == null || street.trim().isBlank()) throw new IllegalArgumentException("La calle es obligatoria.");
            if (number == null || number.trim().isBlank()) throw new IllegalArgumentException("El número es obligatorio.");

            Person person = getOrCreatePerson(user);

            City city = null;
            if (cityId != null) {
                city = cityRepository.findById(cityId).orElse(null);
            }
            if (city == null && cityName != null && !cityName.trim().isBlank()) {
                city = cityRepository.findByNameIgnoreCaseAndDeletedFalse(cityName.trim()).orElse(null);
            }
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

            person.getAddress().add(address);
            personRepository.save(person);

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
        User user = userService.getByEmail(authentication.getName());
        if (user == null || user.getPerson() == null) return "redirect:/profile";

        Person person = user.getPerson();
        if (person.getAddress() != null) {
            for (Address a : person.getAddress()) {
                if (a.getId() != null && a.getId().equals(addressId)) {
                    a.setDeleted(true);
                    break;
                }
            }
            personRepository.save(person);
            redirectAttributes.addFlashAttribute("success", "¡Dirección eliminada correctamente!");
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
        User user = userService.getByEmail(authentication.getName());
        if (user == null) return "redirect:/login";

        try {
            Person person = getOrCreatePerson(user);
            ContactType cType = contactType != null ? contactType : ContactType.PERSONAL;
            String obs = (observation != null && !observation.trim().isBlank()) ? observation.trim() : null;

            if ("EMAIL".equalsIgnoreCase(contactCategory)) {
                if (email == null || email.trim().isBlank()) {
                    throw new IllegalArgumentException("El correo electrónico de contacto es obligatorio.");
                }
                ContactEmail ce = new ContactEmail();
                ce.setEmail(email.trim());
                ce.setContactType(cType);
                ce.setObservation(obs != null ? obs : "Email de contacto");
                ce.setDeleted(false);
                person.getContact().add(ce);
            } else {
                if (phoneNumber == null || phoneNumber.trim().isBlank()) {
                    throw new IllegalArgumentException("El número de teléfono es obligatorio.");
                }
                ContactPhone cp = new ContactPhone();
                cp.setPhoneNumber(phoneNumber.trim());
                cp.setPhoneType(phoneType != null ? phoneType : PhoneType.MOBILE);
                cp.setContactType(cType);
                cp.setObservation(obs != null ? obs : "Teléfono de contacto");
                cp.setDeleted(false);
                person.getContact().add(cp);
            }

            personRepository.save(person);
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
        User user = userService.getByEmail(authentication.getName());
        if (user == null || user.getPerson() == null) return "redirect:/profile";

        Person person = user.getPerson();
        if (person.getContact() != null) {
            for (Contact c : person.getContact()) {
                if (c.getId() != null && c.getId().equals(contactId)) {
                    c.setDeleted(true);
                    break;
                }
            }
            personRepository.save(person);
            redirectAttributes.addFlashAttribute("success", "¡Contacto eliminado correctamente!");
        }
        return "redirect:/profile";
    }

    private Person getOrCreatePerson(User user) {
        Person person = user.getPerson();
        if (person == null) {
            String firstName = user.getUsername() != null ? user.getUsername().split("@")[0] : "Usuario";
            if (user.getRole() == Role.CLIENT) {
                Client client = new Client();
                client.setFirstName(firstName);
                client.setLastName("");
                client.setGender(null);
                client.setDateOfBirth(LocalDate.of(2000, 1, 1));
                client.setIdType(IDType.DNI);
                client.setIdNumber(String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L));
                client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                client.setDeleted(false);
                client.setAddress(new ArrayList<>());
                client.setContact(new ArrayList<>());
                person = clientRepository.save(client);
            } else {
                Employee employee = new Employee();
                employee.setFirstName(firstName);
                employee.setLastName("");
                employee.setGender(null);
                employee.setDateOfBirth(LocalDate.of(2000, 1, 1));
                employee.setIdType(IDType.DNI);
                employee.setIdNumber(String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L));
                employee.setDeleted(false);
                employee.setAddress(new ArrayList<>());
                employee.setContact(new ArrayList<>());
                person = employeeRepository.save(employee);
            }
            user.setPerson(person);
            userRepository.save(user);
        }
        if (person.getAddress() == null) {
            person.setAddress(new ArrayList<>());
        }
        if (person.getContact() == null) {
            person.setContact(new ArrayList<>());
        }
        return person;
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
