package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.dto.ClientProfileDTO;
import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.Contact;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.actor.PendingRegistration;
import ingsoftware.zeroshop.entity.actor.Person;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.EmployeeType;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.EmployeeRepository;
import ingsoftware.zeroshop.repository.actor.PendingRegistrationRepository;
import ingsoftware.zeroshop.repository.actor.PersonRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.service.notification.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PersonRepository personRepository,
                       ClientRepository clientRepository,
                       EmployeeRepository employeeRepository,
                       PendingRegistrationRepository pendingRegistrationRepository,
                       PasswordEncoder passwordEncoder,
                       @Autowired(required = false) EmailService emailService) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Verifica si el usuario autenticado en la sesión actual tiene el rol de Administrador.
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Role.ADMIN.name())
                        || grantedAuthority.getAuthority().equals(Role.ADMIN.name()));
    }

    /**
     * Inicia el registro de un nuevo cliente desde el formulario público de registro.
     * Valida los datos y almacena una registración pendiente con su código de activación.
     * NO crea el usuario ni la persona en la base de datos hasta que el código sea verificado.
     */
    @Transactional
    public PendingRegistration registerClient(String firstName,
                                             String lastName,
                                             IDType idType,
                                             String idNumber,
                                             LocalDate dateOfBirth,
                                             String email,
                                             String password) {
        validatePersonNames(firstName, lastName);
        validateEmail(email);
        validatePassword(password);

        if (idType == null) {
            idType = IDType.DNI;
        }

        if (idNumber == null || idNumber.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        String cleanIdNumber = idNumber.trim();

        if (dateOfBirth == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser una fecha futura.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // 1. Verificar si el usuario ya existe y está activo
        Optional<User> existingUserOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (existingUserOpt.isPresent() && !Boolean.TRUE.equals(existingUserOpt.get().getDeleted())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        // 2. Verificar si la persona ya existe en la BBDD y si ya superó el límite de usuarios
        Optional<Person> existingPersonOpt = personRepository.findByIdNumber(cleanIdNumber);
        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();
            long activeUsersCount = userRepository.countByPersonIdAndDeletedFalse(existingPerson.getId());
            boolean isSameUserReactivating = existingUserOpt.isPresent()
                    && existingUserOpt.get().getPerson() != null
                    && existingUserOpt.get().getPerson().getId().equals(existingPerson.getId());

            if (activeUsersCount >= 2 && !isSameUserReactivating) {
                throw new IllegalArgumentException("La persona con documento " + cleanIdNumber + " ya posee el límite máximo de 2 usuarios vinculados.");
            }
        }

        // 3. Generar código de activación de 6 dígitos
        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));

        // 4. Guardar o actualizar la solicitud de registro pendiente
        PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(PendingRegistration::new);
        pending.setEmail(normalizedEmail);
        pending.setFirstName(firstName.trim());
        pending.setLastName(lastName.trim());
        pending.setIdType(idType);
        pending.setIdNumber(cleanIdNumber);
        pending.setDateOfBirth(dateOfBirth);
        pending.setPassword(passwordEncoder.encode(password));
        pending.setVerificationCode(verificationCode);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setExpiresAt(LocalDateTime.now().plusHours(24));

        PendingRegistration savedPending = pendingRegistrationRepository.save(pending);

        // 5. Enviar código de activación por correo electrónico
        if (emailService != null) {
            emailService.sendVerificationCodeEmail(normalizedEmail, verificationCode, "http://localhost:8080/verify?email=" + normalizedEmail);
        }

        return savedPending;
    }

    /**
     * Sobrecarga de registro con datos de persona por defecto (compatibilidad).
     */
    @Transactional
    public PendingRegistration registerClient(String firstName, String lastName, String email, String password) {
        return registerClient(firstName, lastName, IDType.DNI, generateUniqueIdNumber(), LocalDate.of(2000, 1, 1), email, password);
    }

    /**
     * Verifica la cuenta del cliente validando el código de 6 dígitos enviado por correo.
     * Al validar correctamente el código, crea de forma efectiva la Persona/Cliente y el Usuario en la base de datos.
     */
    @Transactional
    public boolean verifyAccount(String email, String code) {
        if (email == null || code == null || code.isBlank()) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String cleanCode = code.trim();

        // 1. Buscar en registros pendientes
        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail);
        if (pendingOpt.isPresent()) {
            PendingRegistration pending = pendingOpt.get();

            if (pending.getVerificationCode() != null && pending.getVerificationCode().equals(cleanCode)) {
                if (pending.getExpiresAt() != null && pending.getExpiresAt().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("El código de activación ha expirado. Por favor solicita uno nuevo.");
                }

                // Crear y persistir el usuario y su persona asociada
                completeRegistrationFromPending(pending);
                pendingRegistrationRepository.delete(pending);
                return true;
            } else {
                return false;
            }
        }

        // 2. Fallback para cuentas legadas que pudieran tener el código pendiente directamente en User
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getVerificationCode() != null && user.getVerificationCode().trim().equals(cleanCode)) {
                user.setVerified(true);
                user.setVerificationCode(null);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }

    /**
     * Completa el registro creando la persona (Client) y el User en la base de datos tras verificar el código.
     */
    private User completeRegistrationFromPending(PendingRegistration pending) {
        String cleanIdNumber = pending.getIdNumber();
        String normalizedEmail = pending.getEmail();

        Optional<Person> existingPersonOpt = personRepository.findByIdNumber(cleanIdNumber);
        Person personToLink;

        Optional<User> existingUserOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);

        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();

            long activeUsersCount = userRepository.countByPersonIdAndDeletedFalse(existingPerson.getId());
            boolean isSameUserReactivating = existingUserOpt.isPresent()
                    && existingUserOpt.get().getPerson() != null
                    && existingUserOpt.get().getPerson().getId().equals(existingPerson.getId());

            if (activeUsersCount >= 2 && !isSameUserReactivating) {
                throw new IllegalArgumentException("La persona con documento " + cleanIdNumber + " ya posee el límite máximo de 2 usuarios vinculados.");
            }

            existingPerson.setDeleted(false);
            if (isSameUserReactivating) {
                existingPerson.setFirstName(pending.getFirstName());
                existingPerson.setLastName(pending.getLastName());
                existingPerson.setDateOfBirth(pending.getDateOfBirth());
                existingPerson.setIdType(pending.getIdType());
                personToLink = personRepository.save(existingPerson);
            } else {
                personToLink = existingPerson;
            }
        } else {
            Client newClient = new Client();
            newClient.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            newClient.setFirstName(pending.getFirstName());
            newClient.setLastName(pending.getLastName());
            newClient.setDateOfBirth(pending.getDateOfBirth());
            newClient.setIdType(pending.getIdType());
            newClient.setIdNumber(cleanIdNumber);
            newClient.setDeleted(false);
            personToLink = clientRepository.save(newClient);
        }

        User userToSave;
        if (existingUserOpt.isPresent()) {
            userToSave = existingUserOpt.get();
            userToSave.setPerson(personToLink);
            userToSave.setPassword(pending.getPassword());
            userToSave.setRole(Role.CLIENT);
            userToSave.setVerificationCode(null);
            userToSave.setVerified(true);
            userToSave.setDeleted(false);
        } else {
            userToSave = new User();
            userToSave.setUsername(normalizedEmail);
            userToSave.setPassword(pending.getPassword());
            userToSave.setRole(Role.CLIENT);
            userToSave.setPerson(personToLink);
            userToSave.setVerificationCode(null);
            userToSave.setVerified(true);
            userToSave.setDeleted(false);
        }

        User savedUser = userRepository.save(userToSave);

        if (emailService != null) {
            emailService.sendWelcomeEmail(normalizedEmail, pending.getFirstName());
        }

        return savedUser;
    }

    /**
     * Reenvía un nuevo código de activación al correo del usuario para su solicitud pendiente.
     */
    @Transactional
    public String resendVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es requerido.");
        }
        String normalizedEmail = email.trim().toLowerCase();

        Optional<PendingRegistration> pendingOpt = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail);
        if (pendingOpt.isPresent()) {
            PendingRegistration pending = pendingOpt.get();
            String newCode = String.format("%06d", new java.util.Random().nextInt(999999));
            pending.setVerificationCode(newCode);
            pending.setExpiresAt(LocalDateTime.now().plusHours(24));
            pendingRegistrationRepository.save(pending);

            if (emailService != null) {
                emailService.sendVerificationCodeEmail(normalizedEmail, newCode, "http://localhost:8080/verify?email=" + normalizedEmail);
            }
            return newCode;
        }

        // Fallback para usuarios ya existentes no verificados
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getVerified())) {
                throw new IllegalArgumentException("La cuenta ya se encuentra verificada. Puedes iniciar sesión.");
            }
            String newCode = String.format("%06d", new java.util.Random().nextInt(999999));
            user.setVerificationCode(newCode);
            userRepository.save(user);

            if (emailService != null) {
                emailService.sendVerificationCodeEmail(normalizedEmail, newCode, "http://localhost:8080/verify?email=" + normalizedEmail);
            }
            return newCode;
        }

        throw new IllegalArgumentException("No se encontró ninguna solicitud de registro pendiente para el correo: " + email);
    }

    /**
     * Crea un nuevo usuario y su persona correspondiente según el rol solicitado.
     * Si existía un usuario borrado lógicamente con el mismo correo, se reactiva.
     * Regla estricta: Solo los administradores pueden crear usuarios con rol ADMIN.
     */
    @Transactional
    public User createUser(String firstName,
                           String lastName,
                           IDType idType,
                           String idNumber,
                           LocalDate dateOfBirth,
                           String username,
                           String password,
                           Role role) {
        if (role == null) {
            role = Role.CLIENT;
        }

        // Restricción de seguridad: Solo los administradores pueden crear administradores
        if (role == Role.ADMIN && !isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden crear usuarios con rol ADMIN.");
        }

        validatePersonNames(firstName, lastName);
        validateEmail(username);
        validatePassword(password);

        if (idType == null) {
            idType = IDType.DNI;
        }

        if (idNumber == null || idNumber.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        String cleanIdNumber = idNumber.trim();

        if (dateOfBirth == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser una fecha futura.");
        }

        String normalizedUsername = username.trim().toLowerCase();

        Optional<User> existingUserOpt = userRepository.findByUsernameIgnoreCase(normalizedUsername);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getDeleted() != null && !existingUser.getDeleted()) {
                throw new IllegalArgumentException("El usuario o correo electrónico ya se encuentra registrado.");
            }

            // Usuario previamente borrado -> Reactivar
            Optional<Person> existingPersonOpt = personRepository.findByIdNumber(cleanIdNumber);
            Person personToLink;
            if (existingPersonOpt.isPresent()) {
                Person existingPerson = existingPersonOpt.get();
                long activeUsersCount = userRepository.countByPersonIdAndDeletedFalse(existingPerson.getId());
                boolean isSameUserReactivating = existingUser.getPerson() != null
                        && existingUser.getPerson().getId().equals(existingPerson.getId());

                if (activeUsersCount >= 2 && !isSameUserReactivating) {
                    throw new IllegalArgumentException("La persona con documento " + cleanIdNumber + " ya posee el límite máximo de 2 usuarios vinculados.");
                }
                existingPerson.setDeleted(false);
                personToLink = existingPerson;
            } else {
                personToLink = prepareOrReactivatePerson(existingUser.getPerson(), cleanIdNumber, firstName, lastName, dateOfBirth, idType, role);
            }

            existingUser.setPerson(personToLink);
            existingUser.setPassword(passwordEncoder.encode(password));
            existingUser.setRole(role);
            existingUser.setDeleted(false);
            return userRepository.save(existingUser);
        }

        // Usuario nuevo: verificar si la persona ya existe en la base de datos (sea Client o Employee)
        Optional<Person> existingPersonOpt = personRepository.findByIdNumber(cleanIdNumber);
        Person personToLink;
        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();
            long activeUsersCount = userRepository.countByPersonIdAndDeletedFalse(existingPerson.getId());
            if (activeUsersCount >= 2) {
                throw new IllegalArgumentException("La persona con documento " + cleanIdNumber + " ya posee el límite máximo de 2 usuarios vinculados.");
            }
            existingPerson.setDeleted(false);
            personToLink = existingPerson;
        } else {
            personToLink = prepareOrReactivatePerson(null, cleanIdNumber, firstName, lastName, dateOfBirth, idType, role);
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPerson(personToLink);
        user.setDeleted(false);

        return userRepository.save(user);
    }

    /**
     * Prepara, reactiva o crea la entidad concreta de Persona (Client o Employee).
     */
    private Person prepareOrReactivatePerson(Person currentPerson,
                                             String cleanIdNumber,
                                             String firstName,
                                             String lastName,
                                             LocalDate dateOfBirth,
                                             IDType idType,
                                             Role role) {
        if (role == Role.CLIENT) {
            Client client;
            if (currentPerson instanceof Client existingClient) {
                client = existingClient;
            } else {
                Optional<Person> personWithDoc = personRepository.findByIdNumber(cleanIdNumber);
                if (personWithDoc.isPresent() && personWithDoc.get() instanceof Client existingClient) {
                    client = existingClient;
                } else {
                    client = new Client();
                    client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
            }
            client.setFirstName(firstName.trim());
            client.setLastName(lastName.trim());
            client.setDateOfBirth(dateOfBirth);
            client.setIdType(idType);
            client.setIdNumber(cleanIdNumber);
            client.setDeleted(false);
            return clientRepository.save(client);
        } else {
            Employee employee;
            if (currentPerson instanceof Employee existingEmp) {
                employee = existingEmp;
            } else {
                Optional<Person> personWithDoc = personRepository.findByIdNumber(cleanIdNumber);
                if (personWithDoc.isPresent() && personWithDoc.get() instanceof Employee existingEmp) {
                    employee = existingEmp;
                } else {
                    employee = new Employee();
                    employee.setHireDate(LocalDate.now());
                }
            }
            employee.setFirstName(firstName.trim());
            employee.setLastName(lastName.trim());
            employee.setDateOfBirth(dateOfBirth);
            employee.setIdType(idType);
            employee.setIdNumber(cleanIdNumber);
            employee.setEmployeeType(role == Role.ADMIN ? EmployeeType.MANAGER : EmployeeType.OTHER);
            if (employee.getHireDate() == null) {
                employee.setHireDate(LocalDate.now());
            }
            employee.setDeleted(false);
            return employeeRepository.save(employee);
        }
    }

    /**
     * Sobrecarga de creación de usuario con datos por defecto de persona (compatibilidad).
     */
    @Transactional
    public User createUser(String firstName, String lastName, String username, String password, Role role) {
        return createUser(firstName, lastName, IDType.DNI, generateUniqueIdNumber(), LocalDate.of(2000, 1, 1), username, password, role);
    }

    /**
     * Crea una persona y un usuario a partir de entidades previamente preparadas.
     * Si el rol es ADMIN, valida que el creador sea un administrador.
     */
    @Transactional
    public User createPersonAndUser(Person person, User user) {
        if (user == null || person == null) {
            throw new IllegalArgumentException("El usuario y la persona son obligatorios.");
        }

        if (user.getRole() == Role.ADMIN && !isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden crear usuarios con rol ADMIN.");
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        String normalizedUsername = user.getUsername().trim().toLowerCase();
        Optional<User> existingUserOpt = userRepository.findByUsernameIgnoreCase(normalizedUsername);
        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            if (existing.getDeleted() != null && !existing.getDeleted()) {
                throw new IllegalArgumentException("El usuario ya se encuentra registrado.");
            }
        }

        // Validar y asegurar datos mínimos de la persona
        if (person.getFirstName() == null || person.getFirstName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio.");
        }
        if (person.getLastName() == null || person.getLastName().isBlank()) {
            throw new IllegalArgumentException("El apellido de la persona es obligatorio.");
        }
        if (person.getIdType() == null) {
            person.setIdType(IDType.DNI);
        }
        if (person.getIdNumber() == null || person.getIdNumber().isBlank()) {
            person.setIdNumber(generateUniqueIdNumber());
        }
        if (person.getDateOfBirth() == null) {
            person.setDateOfBirth(LocalDate.of(2000, 1, 1));
        }
        person.setDeleted(false);

        Person savedPerson;
        Optional<Person> existingPersonOpt = personRepository.findByIdNumber(person.getIdNumber().trim());
        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();
            long activeUsersCount = userRepository.countByPersonIdAndDeletedFalse(existingPerson.getId());
            boolean isSameUserReactivating = existingUserOpt.isPresent()
                    && existingUserOpt.get().getPerson() != null
                    && existingUserOpt.get().getPerson().getId().equals(existingPerson.getId());

            if (activeUsersCount >= 2 && !isSameUserReactivating) {
                throw new IllegalArgumentException("La persona con documento " + person.getIdNumber() + " ya posee el límite máximo de 2 usuarios vinculados.");
            }
            existingPerson.setDeleted(false);
            savedPerson = existingPerson;
        } else if (person instanceof Client client) {
            if (client.getClientNumber() == null || client.getClientNumber().isBlank()) {
                client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            savedPerson = clientRepository.save(client);
        } else if (person instanceof Employee employee) {
            if (employee.getEmployeeType() == null) {
                employee.setEmployeeType(EmployeeType.OTHER);
            }
            if (employee.getHireDate() == null) {
                employee.setHireDate(LocalDate.now());
            }
            savedPerson = employeeRepository.save(employee);
        } else {
            throw new IllegalArgumentException("La persona debe ser una instancia de Client o Employee.");
        }

        // Codificar contraseña si no está encriptada
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            existing.setPassword(user.getPassword());
            existing.setRole(user.getRole() != null ? user.getRole() : Role.CLIENT);
            existing.setPerson(savedPerson);
            existing.setDeleted(false);
            return userRepository.save(existing);
        }

        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }
        user.setUsername(normalizedUsername);
        user.setDeleted(false);
        user.setPerson(savedPerson);
        return userRepository.save(user);
    }

    /**
     * Lista todos los usuarios activos del sistema.
     */
    public List<User> findAll() {
        return userRepository.findAllByDeletedFalse();
    }

    /**
     * Busca un usuario activo por su ID único.
     */
    public Optional<User> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * Busca un usuario activo por su correo electrónico / username.
     */
    public User getByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByUsernameIgnoreCaseAndDeletedFalse(email.trim())
                .orElse(null);
    }

    /**
     * Obtiene el nombre de pila del usuario a partir de su correo electrónico.
     */
    public String getUserFirstName(String email) {
        User user = getByEmail(email);
        if (user != null) {
            if (user.getPerson() != null && user.getPerson().getFirstName() != null && !user.getPerson().getFirstName().isBlank()) {
                return user.getPerson().getFirstName();
            }
            if (user.getUsername() != null) {
                return user.getUsername().split("@")[0];
            }
        }
        return "";
    }

    /**
     * Actualiza el perfil de un usuario existente.
     */
    @Transactional
    public User updateProfile(UUID id, String firstName, String lastName, String email, String password) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        validatePersonNames(firstName, lastName);
        validateEmail(email);

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> otherUser = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (otherUser.isPresent() && !otherUser.get().getId().equals(id) && (otherUser.get().getDeleted() == null || !otherUser.get().getDeleted())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado por otro usuario.");
        }
        user.setUsername(normalizedEmail);

        if (password != null && !password.isBlank()) {
            validatePassword(password);
            user.setPassword(passwordEncoder.encode(password));
        }

        Person person = user.getPerson();
        if (person == null) {
            if (user.getRole() == Role.CLIENT) {
                Client client = new Client();
                client.setFirstName(firstName.trim());
                client.setLastName(lastName.trim());
                client.setDateOfBirth(LocalDate.of(2000, 1, 1));
                client.setIdType(IDType.DNI);
                client.setIdNumber(generateUniqueIdNumber());
                client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                client.setDeleted(false);
                person = clientRepository.save(client);
            } else {
                Employee employee = new Employee();
                employee.setFirstName(firstName.trim());
                employee.setLastName(lastName.trim());
                employee.setDateOfBirth(LocalDate.of(2000, 1, 1));
                employee.setIdType(IDType.DNI);
                employee.setIdNumber(generateUniqueIdNumber());
                employee.setEmployeeType(user.getRole() == Role.ADMIN ? EmployeeType.MANAGER : EmployeeType.OTHER);
                employee.setHireDate(LocalDate.now());
                employee.setDeleted(false);
                person = employeeRepository.save(employee);
            }
            user.setPerson(person);
        } else {
            person.setFirstName(firstName.trim());
            person.setLastName(lastName.trim());
            personRepository.save(person);
        }

        return userRepository.save(user);
    }

    /**
     * Actualiza el perfil de un usuario garantizando que no sea Administrador.
     */
    @Transactional
    public void updateNonAdminProfile(UUID id, String firstName, String lastName, String email, String password) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("No se permite modificar un usuario administrador desde esta sección.");
        }

        updateProfile(id, firstName, lastName, email, password);
    }

    /**
     * Actualiza todos los campos de un usuario desde la administración (incluyendo rol).
     * Solo los administradores pueden asignar el rol ADMIN.
     */
    @Transactional
    public User updateUser(UUID id, String firstName, String lastName, String username, String password, Role role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (role == Role.ADMIN && user.getRole() != Role.ADMIN && !isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden asignar el rol ADMIN.");
        }

        validatePersonNames(firstName, lastName);
        validateEmail(username);

        String normalizedUsername = username.trim().toLowerCase();
        Optional<User> otherUser = userRepository.findByUsernameIgnoreCase(normalizedUsername);
        if (otherUser.isPresent() && !otherUser.get().getId().equals(id) && (otherUser.get().getDeleted() == null || !otherUser.get().getDeleted())) {
            throw new IllegalArgumentException("El usuario o correo electrónico ya está registrado.");
        }
        user.setUsername(normalizedUsername);

        if (role != null) {
            user.setRole(role);
        }

        if (password != null && !password.isBlank()) {
            validatePassword(password);
            user.setPassword(passwordEncoder.encode(password));
        }

        Person person = user.getPerson();
        if (person == null) {
            if (user.getRole() == Role.CLIENT) {
                Client client = new Client();
                client.setFirstName(firstName.trim());
                client.setLastName(lastName.trim());
                client.setDateOfBirth(LocalDate.of(2000, 1, 1));
                client.setIdType(IDType.DNI);
                client.setIdNumber(generateUniqueIdNumber());
                client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                client.setDeleted(false);
                person = clientRepository.save(client);
            } else {
                Employee employee = new Employee();
                employee.setFirstName(firstName.trim());
                employee.setLastName(lastName.trim());
                employee.setDateOfBirth(LocalDate.of(2000, 1, 1));
                employee.setIdType(IDType.DNI);
                employee.setIdNumber(generateUniqueIdNumber());
                employee.setEmployeeType(user.getRole() == Role.ADMIN ? EmployeeType.MANAGER : EmployeeType.OTHER);
                employee.setHireDate(LocalDate.now());
                employee.setDeleted(false);
                person = employeeRepository.save(employee);
            }
            user.setPerson(person);
        } else {
            person.setFirstName(firstName.trim());
            person.setLastName(lastName.trim());
            personRepository.save(person);
        }

        return userRepository.save(user);
    }

    /**
     * Desactiva lógicamente un usuario si no es Administrador.
     */
    @Transactional
    public void deleteNonAdmin(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("No se puede eliminar un usuario con rol de Administrador.");
        }

        user.setDeleted(true);
        userRepository.save(user);

        if (user.getPerson() != null) {
            long remainingActiveUsers = userRepository.countByPersonIdAndDeletedFalse(user.getPerson().getId());
            if (remainingActiveUsers == 0) {
                user.getPerson().setDeleted(true);
                personRepository.save(user.getPerson());
            }
        }
    }

    /**
     * Desactiva lógicamente un usuario por su ID.
     * Si el usuario a eliminar es ADMIN, valida que quien lo elimina sea un administrador.
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (user.getRole() == Role.ADMIN && !isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden eliminar usuarios administradores.");
        }

        user.setDeleted(true);
        userRepository.save(user);

        if (user.getPerson() != null) {
            long remainingActiveUsers = userRepository.countByPersonIdAndDeletedFalse(user.getPerson().getId());
            if (remainingActiveUsers == 0) {
                user.getPerson().setDeleted(true);
                personRepository.save(user.getPerson());
            }
        }
    }

    private void validatePersonNames(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("El correo electrónico tiene un formato inválido.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
    }

    private String generateUniqueIdNumber() {
        String candidate;
        do {
            long num = Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L;
            candidate = String.valueOf(num);
        } while (personRepository.existsByIdNumber(candidate));
        return candidate;
    }

    public List<Client> getAllActiveClients() {
        return clientRepository.findAllByDeletedFalse();
    }

    public Optional<Client> findClientByDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return Optional.empty();
        }
        String cleanDni = dni.trim();
        return clientRepository.findAllByDeletedFalse().stream()
                .filter(c -> cleanDni.equalsIgnoreCase(c.getIdNumber()))
                .findFirst();
    }

    @Transactional
    public Person getOrCreatePerson(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo.");
        }
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

    @Transactional
    public User updateUserProfile(String currentUsername, ClientProfileDTO form) {
        User user = getByEmail(currentUsername);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

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

        if (form.getEmailPromotionsEnabled() != null) {
            user.setEmailPromotionsEnabled(form.getEmailPromotionsEnabled());
        }

        personRepository.save(person);
        return userRepository.save(user);
    }

    @Transactional
    public boolean toggleEmailPromotions(String username, Boolean enabled) {
        User user = getByEmail(username);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        boolean newStatus = (enabled != null) ? enabled : !(user.getEmailPromotionsEnabled() == null || user.getEmailPromotionsEnabled());
        user.setEmailPromotionsEnabled(newStatus);
        userRepository.save(user);
        return newStatus;
    }

    @Transactional
    public void addAddressToUserProfile(String username, String street, String number, String floor,
                                       String apartment, String zipCode, String observations,
                                       City city) {
        User user = getByEmail(username);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        if (street == null || street.trim().isBlank()) throw new IllegalArgumentException("La calle es obligatoria.");
        if (number == null || number.trim().isBlank()) throw new IllegalArgumentException("El número es obligatorio.");

        Person person = getOrCreatePerson(user);

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
    }

    @Transactional
    public void deleteAddressFromUserProfile(String username, UUID addressId) {
        User user = getByEmail(username);
        if (user == null || user.getPerson() == null) return;
        Person person = user.getPerson();
        if (person.getAddress() != null) {
            for (Address a : person.getAddress()) {
                if (a.getId() != null && a.getId().equals(addressId)) {
                    a.setDeleted(true);
                    break;
                }
            }
            personRepository.save(person);
        }
    }

    @Transactional
    public void addContactToUserProfile(String username, String contactCategory, String phoneNumber,
                                       PhoneType phoneType, String email,
                                       ContactType contactType, String observation) {
        User user = getByEmail(username);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
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
    }

    @Transactional
    public void deleteContactFromUserProfile(String username, UUID contactId) {
        User user = getByEmail(username);
        if (user == null || user.getPerson() == null) return;
        Person person = user.getPerson();
        if (person.getContact() != null) {
            for (Contact c : person.getContact()) {
                if (c.getId() != null && c.getId().equals(contactId)) {
                    c.setDeleted(true);
                    break;
                }
            }
            personRepository.save(person);
        }
    }
}
