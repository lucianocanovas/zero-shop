package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.actor.Person;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.EmployeeType;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.EmployeeRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PersonRepository personRepository,
                       ClientRepository clientRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       @Autowired(required = false) EmailService emailService) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
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
     * Registra un nuevo cliente desde el formulario público de registro con todos sus datos de persona.
     * Si existía un usuario borrado lógicamente con el mismo correo, se reactiva con los nuevos datos.
     */
    @Transactional
    public User registerClient(String firstName,
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

        // Verificar si el usuario ya existe
        Optional<User> existingUserOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getDeleted() != null && !existingUser.getDeleted()) {
                throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
            }

            // El usuario existe pero estaba eliminado lógicamente -> Reactivar
            Person currentPerson = existingUser.getPerson();
            UUID currentPersonId = currentPerson != null ? currentPerson.getId() : null;
            if (currentPersonId != null) {
                if (personRepository.existsByIdNumberAndDeletedFalseAndIdNot(cleanIdNumber, currentPersonId)) {
                    throw new IllegalArgumentException("El número de documento ya se encuentra registrado por otro usuario.");
                }
            } else {
                if (personRepository.existsByIdNumberAndDeletedFalse(cleanIdNumber)) {
                    throw new IllegalArgumentException("El número de documento ya se encuentra registrado.");
                }
            }

            Person savedPerson = prepareOrReactivatePerson(currentPerson, cleanIdNumber, firstName, lastName, dateOfBirth, idType, Role.CLIENT);

            String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
            existingUser.setPerson(savedPerson);
            existingUser.setPassword(passwordEncoder.encode(password));
            existingUser.setRole(Role.CLIENT);
            existingUser.setVerificationCode(verificationCode);
            existingUser.setVerified(false);
            existingUser.setDeleted(false);
            User savedUser = userRepository.save(existingUser);

            if (emailService != null) {
                emailService.sendVerificationCodeEmail(normalizedEmail, verificationCode, "http://localhost:8080/verify?email=" + normalizedEmail);
                emailService.sendWelcomeEmail(normalizedEmail, firstName.trim());
            }

            return savedUser;
        }

        // Si es un usuario nuevo, validar que el documento no esté en uso por otra persona activa
        if (personRepository.existsByIdNumberAndDeletedFalse(cleanIdNumber)) {
            throw new IllegalArgumentException("El número de documento ya se encuentra registrado.");
        }

        Person savedPerson = prepareOrReactivatePerson(null, cleanIdNumber, firstName, lastName, dateOfBirth, idType, Role.CLIENT);

        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
        User user = new User();
        user.setUsername(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CLIENT);
        user.setPerson(savedPerson);
        user.setVerificationCode(verificationCode);
        user.setVerified(false);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);

        if (emailService != null) {
            emailService.sendVerificationCodeEmail(normalizedEmail, verificationCode, "http://localhost:8080/verify?email=" + normalizedEmail);
            emailService.sendWelcomeEmail(normalizedEmail, firstName.trim());
        }

        return savedUser;
    }

    /**
     * Verifica la cuenta del cliente validando el código de 6 dígitos enviado por correo.
     */
    @Transactional
    public boolean verifyAccount(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(normalizedEmail);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (user.getVerificationCode() != null && user.getVerificationCode().trim().equals(code.trim())) {
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Reenvía un nuevo código de activación al correo del usuario.
     */
    @Transactional
    public String resendVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es requerido.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByUsernameIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el correo: " + email));

        String newCode = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationCode(newCode);
        userRepository.save(user);

        if (emailService != null) {
            emailService.sendVerificationCodeEmail(normalizedEmail, newCode, "http://localhost:8080/verify?email=" + normalizedEmail);
        }
        return newCode;
    }

    /**
     * Sobrecarga de registro con datos de persona por defecto (compatibilidad).
     */
    @Transactional
    public User registerClient(String firstName, String lastName, String email, String password) {
        return registerClient(firstName, lastName, IDType.DNI, generateUniqueIdNumber(), LocalDate.of(2000, 1, 1), email, password);
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
            Person currentPerson = existingUser.getPerson();
            UUID currentPersonId = currentPerson != null ? currentPerson.getId() : null;
            if (currentPersonId != null) {
                if (personRepository.existsByIdNumberAndDeletedFalseAndIdNot(cleanIdNumber, currentPersonId)) {
                    throw new IllegalArgumentException("El número de documento ya se encuentra registrado por otro usuario.");
                }
            } else {
                if (personRepository.existsByIdNumberAndDeletedFalse(cleanIdNumber)) {
                    throw new IllegalArgumentException("El número de documento ya se encuentra registrado.");
                }
            }

            Person savedPerson = prepareOrReactivatePerson(currentPerson, cleanIdNumber, firstName, lastName, dateOfBirth, idType, role);

            existingUser.setPerson(savedPerson);
            existingUser.setPassword(passwordEncoder.encode(password));
            existingUser.setRole(role);
            existingUser.setDeleted(false);
            return userRepository.save(existingUser);
        }

        if (personRepository.existsByIdNumberAndDeletedFalse(cleanIdNumber)) {
            throw new IllegalArgumentException("El número de documento ya se encuentra registrado.");
        }

        Person savedPerson = prepareOrReactivatePerson(null, cleanIdNumber, firstName, lastName, dateOfBirth, idType, role);

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPerson(savedPerson);
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
        if (person instanceof Client client) {
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
        if (user.getPerson() != null) {
            user.getPerson().setDeleted(true);
            personRepository.save(user.getPerson());
        }
        userRepository.save(user);
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
        if (user.getPerson() != null) {
            user.getPerson().setDeleted(true);
            personRepository.save(user.getPerson());
        }
        userRepository.save(user);
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
}
