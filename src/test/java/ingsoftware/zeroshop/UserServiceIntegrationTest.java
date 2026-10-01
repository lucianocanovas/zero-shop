package ingsoftware.zeroshop;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.service.actor.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ingsoftware.zeroshop.repository.actor.PersonRepository personRepository;

    @Autowired
    private ingsoftware.zeroshop.repository.actor.ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Registro de usuario: Crea automáticamente una Persona (Client) y el Usuario asociado")
    public void testRegisterClientCreatesUserAndPerson() {
        String email = "nuevo.cliente." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        User createdUser = userService.registerClient("Carlos", "Gomez", email, "SecurePass123");

        assertNotNull(createdUser, "El usuario creado no debe ser nulo");
        assertNotNull(createdUser.getId(), "El usuario debe tener un ID asignado");
        assertEquals(email.toLowerCase(), createdUser.getUsername());
        assertEquals(Role.CLIENT, createdUser.getRole(), "El rol por defecto de registro debe ser CLIENT");
        assertTrue(passwordEncoder.matches("SecurePass123", createdUser.getPassword()), "La contraseña debe estar hasheada con BCrypt");

        // Validar que se creó la Persona y es de tipo Client
        assertNotNull(createdUser.getPerson(), "El usuario debe tener una Persona asociada");
        assertEquals("Carlos", createdUser.getPerson().getFirstName());
        assertEquals("Gomez", createdUser.getPerson().getLastName());
        assertNotNull(createdUser.getPerson().getIdNumber(), "La persona debe tener un número de identificación");
        assertNotNull(createdUser.getPerson().getIdType(), "La persona debe tener un tipo de identificación");

        assertTrue(createdUser.getPerson() instanceof Client, "La persona creada para un CLIENT debe ser instancia de Client");
        Client client = (Client) createdUser.getPerson();
        assertNotNull(client.getClientNumber(), "El cliente debe tener un número de cliente autogenerado");
    }

    @Test
    @DisplayName("Registro de cliente: Permite registrar con datos completos de persona (idType, idNumber, dateOfBirth)")
    public void testRegisterClientWithFullDetails() {
        String email = "cliente.completo." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String idNumber = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L);
        LocalDate dob = LocalDate.of(1998, 7, 15);

        User createdUser = userService.registerClient("Maria", "Rodriguez", IDType.PASAPORTE, idNumber, dob, email, "PasswordFull123");

        assertNotNull(createdUser);
        assertEquals(email.toLowerCase(), createdUser.getUsername());
        assertEquals(Role.CLIENT, createdUser.getRole());

        assertNotNull(createdUser.getPerson());
        assertTrue(createdUser.getPerson() instanceof Client);
        Client client = (Client) createdUser.getPerson();
        assertEquals("Maria", client.getFirstName());
        assertEquals("Rodriguez", client.getLastName());
        assertEquals(IDType.PASAPORTE, client.getIdType());
        assertEquals(idNumber, client.getIdNumber());
        assertEquals(dob, client.getDateOfBirth());
        assertNotNull(client.getClientNumber());
    }

    @Test
    @DisplayName("Borrado lógico: Reactiva una cuenta borrada si se vuelve a registrar con el mismo correo")
    public void testRegisterClientReactivatesDeletedAccountWithSameEmail() {
        String email = "reactivar.cliente." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String idNumber = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L);
        LocalDate dob = LocalDate.of(1995, 5, 20);

        // 1. Crear usuario original
        User originalUser = userService.registerClient("Pedro", "Picapiedra", IDType.DNI, idNumber, dob, email, "ClaveVieja123");
        assertNotNull(originalUser);
        assertFalse(originalUser.getDeleted());

        // 2. Eliminar lógicamente el usuario
        userService.deleteNonAdmin(originalUser.getId());
        User deletedUser = userRepository.findById(originalUser.getId()).orElseThrow();
        assertTrue(deletedUser.getDeleted(), "El usuario debe quedar marcado como borrado");
        assertTrue(deletedUser.getPerson().getDeleted(), "La persona debe quedar marcada como borrada");

        // 3. Volver a registrar con el MISMO correo y DNI
        User reactivatedUser = userService.registerClient("Pedro Renacido", "Picapiedra", IDType.DNI, idNumber, dob, email, "NuevaClave456");

        // 4. Validar que no arrojó error, reutilizó la fila y está reactivada
        assertNotNull(reactivatedUser);
        assertEquals(originalUser.getId(), reactivatedUser.getId(), "Debe reutilizar el mismo registro de usuario");
        assertFalse(reactivatedUser.getDeleted(), "El usuario debe volver a estar activo");
        assertFalse(reactivatedUser.getPerson().getDeleted(), "La persona debe volver a estar activa");
        assertEquals("Pedro Renacido", reactivatedUser.getPerson().getFirstName());
        assertTrue(passwordEncoder.matches("NuevaClave456", reactivatedUser.getPassword()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin: Puede crear cualquier usuario con datos completos de Persona")
    public void testAdminCanCreateUserWithFullPersonDetails() {
        String email = "empleado.admin." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String idNumber = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 90000000L + 10000000L);
        LocalDate dob = LocalDate.of(1992, 3, 22);

        User createdUser = userService.createUser("Laura", "Sanchez", IDType.CEDULA, idNumber, dob, email, "PassAdminCreated123", Role.EMPLOYEE);

        assertNotNull(createdUser);
        assertEquals(email.toLowerCase(), createdUser.getUsername());
        assertEquals(Role.EMPLOYEE, createdUser.getRole());
        assertNotNull(createdUser.getPerson());
        assertTrue(createdUser.getPerson() instanceof Employee);
        assertEquals("Laura", createdUser.getPerson().getFirstName());
        assertEquals("Sanchez", createdUser.getPerson().getLastName());
        assertEquals(IDType.CEDULA, createdUser.getPerson().getIdType());
        assertEquals(idNumber, createdUser.getPerson().getIdNumber());
        assertEquals(dob, createdUser.getPerson().getDateOfBirth());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Seguridad: Un Administrador autenticado SÍ puede crear otro usuario con rol ADMIN")
    public void testAdminCanCreateAdminUser() {
        String email = "nuevo.admin." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        User adminUser = userService.createUser("Admin", "Supremo", email, "AdminPass123", Role.ADMIN);

        assertNotNull(adminUser);
        assertEquals(Role.ADMIN, adminUser.getRole());
        assertNotNull(adminUser.getPerson(), "El usuario admin debe tener una persona asociada");
        assertEquals("Admin", adminUser.getPerson().getFirstName());
        assertEquals("Supremo", adminUser.getPerson().getLastName());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("Seguridad: Un usuario con rol CLIENT NO puede crear usuarios con rol ADMIN")
    public void testClientCannotCreateAdminUser() {
        String email = "intento.admin." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.createUser("Hacker", "Intruso", email, "Password123", Role.ADMIN);
        });

        assertTrue(exception.getMessage().contains("Solo los administradores pueden crear usuarios con rol ADMIN"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Seguridad: Un usuario anónimo NO puede crear usuarios con rol ADMIN")
    public void testAnonymousCannotCreateAdminUser() {
        SecurityContextHolder.clearContext();
        String email = "anonimo.admin." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.createUser("Anonimo", "Desconocido", email, "Password123", Role.ADMIN);
        });

        assertTrue(exception.getMessage().contains("Solo los administradores pueden crear usuarios con rol ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin: Puede crear un usuario con rol EMPLOYEE y se crea una Persona (Employee)")
    public void testAdminCanCreateEmployeeUser() {
        String email = "nuevo.empleado." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        User employeeUser = userService.createUser("Ana", "Vendedora", email, "PassEmployee123", Role.EMPLOYEE);

        assertNotNull(employeeUser);
        assertEquals(Role.EMPLOYEE, employeeUser.getRole());
        assertNotNull(employeeUser.getPerson());
        assertTrue(employeeUser.getPerson() instanceof Employee, "La persona debe ser de tipo Employee");
    }

    @Test
    @DisplayName("Validación: No se permite registrar dos usuarios con el mismo correo electrónico si ya está activo")
    public void testDuplicateEmailThrowsException() {
        String email = "duplicado." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";

        userService.registerClient("Primero", "Perez", email, "Password123");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerClient("Segundo", "Lopez", email, "Password456");
        });

        assertTrue(exception.getMessage().contains("ya se encuentra registrado"));
    }

    @Test
    @DisplayName("Actualización: Se actualizan los datos del perfil y de la persona asociada")
    public void testUpdateProfileUpdatesUserAndPerson() {
        String email = "original." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        User user = userService.registerClient("NombreOriginal", "ApellidoOriginal", email, "Password123");

        String newEmail = "modificado." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        User updated = userService.updateProfile(user.getId(), "NombreNuevo", "ApellidoNuevo", newEmail, "NewPassword123");

        assertEquals(newEmail.toLowerCase(), updated.getUsername());
        assertEquals("NombreNuevo", updated.getPerson().getFirstName());
        assertEquals("ApellidoNuevo", updated.getPerson().getLastName());
        assertTrue(passwordEncoder.matches("NewPassword123", updated.getPassword()));
    }

    @Test
    @DisplayName("Persona: Se permite registrar hasta 2 usuarios vinculados a la misma Persona sin duplicar datos")
    public void testPersonCanRegisterUpToTwoUsersWithoutDuplicatingPersonData() {
        String dni = "77" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 6);
        String email1 = "user1." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String email2 = "user2." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String email3 = "user3." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        LocalDate dob = LocalDate.of(1995, 8, 15);

        // 1. Primer registro para esta persona
        User user1 = userService.registerClient("Luciano", "Perez", IDType.DNI, dni, dob, email1, "Clave12345");
        assertNotNull(user1);
        assertNotNull(user1.getPerson());
        UUID personId = user1.getPerson().getId();

        // Verificar que solo existe 1 registro de persona con ese DNI
        assertEquals(1, personRepository.findByIdNumber(dni).stream().count());

        // 2. Segundo registro con OTRO email pero el MISMO DNI
        User user2 = userService.registerClient("Luciano", "Perez", IDType.DNI, dni, dob, email2, "Clave67890");
        assertNotNull(user2);
        assertNotNull(user2.getPerson());
        assertEquals(personId, user2.getPerson().getId(), "El segundo usuario debe estar vinculado a la misma persona");

        // Verificar que NO se duplicaron los datos de la persona
        assertEquals(1, personRepository.findByIdNumber(dni).stream().count(), "No debe haber duplicados en la tabla de personas");
        assertEquals(2, userRepository.countByPersonIdAndDeletedFalse(personId), "Debe haber exactamente 2 usuarios activos vinculados");

        // 3. Tercer intento de registro con el MISMO DNI -> debe fallar por superar el límite de 2
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerClient("Luciano", "Perez", IDType.DNI, dni, dob, email3, "ClaveOtra123");
        });
        assertTrue(ex.getMessage().contains("ya posee el límite máximo de 2 usuarios vinculados"));

        // Verificar que sigue habiendo exactamente 2 usuarios y 1 persona
        assertEquals(2, userRepository.countByPersonIdAndDeletedFalse(personId));
        assertEquals(1, personRepository.findByIdNumber(dni).stream().count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Persona: Si una persona ya existe como Empleado, al registrarse como Cliente se vincula al Empleado existente sin duplicar")
    public void testRegisterClientLinksToExistingEmployeeWithoutCreatingNewPerson() {
        String dni = "88" + String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 6);
        String empEmail = "empleado." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        String clientEmail = "cliente." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        LocalDate dob = LocalDate.of(1990, 4, 10);

        // 1. Crear usuario empleado desde el panel de admin
        User empUser = userService.createUser("Marcos", "Empleado", IDType.DNI, dni, dob, empEmail, "EmpPass123", Role.EMPLOYEE);
        assertNotNull(empUser);
        assertTrue(empUser.getPerson() instanceof Employee, "La persona debe ser de tipo Employee");
        UUID employeePersonId = empUser.getPerson().getId();

        // 2. Registrarse desde la web con ese mismo DNI
        User clientUser = userService.registerClient("Marcos", "Empleado", IDType.DNI, dni, dob, clientEmail, "ClientPass123");
        assertNotNull(clientUser);
        assertEquals(employeePersonId, clientUser.getPerson().getId(), "El cliente debe reutilizar la misma Persona (Employee) ya existente");
        assertEquals(2, userRepository.countByPersonIdAndDeletedFalse(employeePersonId), "Debe haber 2 usuarios vinculados a este empleado");

        // 3. Intentar registrar un 3er usuario para el mismo empleado debe fallar
        String thirdEmail = "tercero." + UUID.randomUUID().toString().substring(0, 6) + "@test.com";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerClient("Marcos", "Empleado", IDType.DNI, dni, dob, thirdEmail, "OtraPass123");
        });
        assertTrue(ex.getMessage().contains("ya posee el límite máximo de 2 usuarios vinculados"));
    }
}
