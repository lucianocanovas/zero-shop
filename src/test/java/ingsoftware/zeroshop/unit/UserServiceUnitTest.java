package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.PersonRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.service.actor.UserService;
import ingsoftware.zeroshop.service.notification.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Unit: Registro de cliente exitoso genera código de activación de 6 dígitos")
    public void testRegisterClientGeneratesVerificationCode() {
        String email = "juan.perez@example.com";
        String rawPass = "Segura123";
        String encodedPass = "$2a$10$encodedHashDummy";

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.empty());
        when(personRepository.findByIdNumber("35123456")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPass)).thenReturn(encodedPass);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerClient(
                "Juan", "Perez", IDType.DNI, "35123456",
                LocalDate.of(1995, 5, 20), email, rawPass
        );

        assertNotNull(registered);
        assertEquals(email, registered.getUsername());
        assertEquals(encodedPass, registered.getPassword());
        assertEquals(Role.CLIENT, registered.getRole());
        assertFalse(registered.getVerified(), "Un cliente nuevo debe crearse como no verificado");
        assertNotNull(registered.getVerificationCode(), "Debe autogenerarse un código de verificación");
        assertEquals(6, registered.getVerificationCode().length(), "El código debe ser de 6 dígitos");

        verify(emailService, times(1)).sendVerificationCodeEmail(eq(email), eq(registered.getVerificationCode()), anyString());
        verify(emailService, times(1)).sendWelcomeEmail(eq(email), eq("Juan"));
    }

    @Test
    @DisplayName("Unit: Registro reutiliza Persona existente sin duplicarla si tiene menos de 2 usuarios")
    public void testRegisterClientReusesExistingPerson() {
        String email = "segundo.usuario@example.com";
        String rawPass = "Segura123";
        String encodedPass = "$2a$10$encodedHashDummy";

        Client existingPerson = new Client();
        java.util.UUID personId = java.util.UUID.randomUUID();
        existingPerson.setId(personId);
        existingPerson.setIdNumber("35123456");
        existingPerson.setFirstName("Juan");
        existingPerson.setLastName("Perez");

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.empty());
        when(personRepository.findByIdNumber("35123456")).thenReturn(Optional.of(existingPerson));
        when(userRepository.countByPersonIdAndDeletedFalse(personId)).thenReturn(1L);
        when(passwordEncoder.encode(rawPass)).thenReturn(encodedPass);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerClient(
                "Juan", "Perez", IDType.DNI, "35123456",
                LocalDate.of(1995, 5, 20), email, rawPass
        );

        assertNotNull(registered);
        assertEquals(email, registered.getUsername());
        assertSame(existingPerson, registered.getPerson(), "Debe vincularse a la misma instancia de Persona existente");
        verify(clientRepository, never()).save(any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Unit: Registro falla si la persona ya posee 2 usuarios activos vinculados")
    public void testRegisterClientFailsWhenLimitOfTwoUsersReached() {
        String email = "tercer.usuario@example.com";
        Client existingPerson = new Client();
        java.util.UUID personId = java.util.UUID.randomUUID();
        existingPerson.setId(personId);
        existingPerson.setIdNumber("35123456");

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.empty());
        when(personRepository.findByIdNumber("35123456")).thenReturn(Optional.of(existingPerson));
        when(userRepository.countByPersonIdAndDeletedFalse(personId)).thenReturn(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerClient("Juan", "Perez", IDType.DNI, "35123456",
                        LocalDate.of(1995, 5, 20), email, "Password123")
        );

        assertTrue(ex.getMessage().contains("ya posee el límite máximo de 2 usuarios vinculados"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Registro de cliente falla si las contraseñas o nombres están vacíos")
    public void testRegisterClientValidations() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerClient("", "Perez", IDType.DNI, "12345678", LocalDate.of(1990, 1, 1), "test@test.com", "123456")
        );

        assertThrows(IllegalArgumentException.class, () ->
                userService.registerClient("Juan", "Perez", IDType.DNI, "12345678", LocalDate.now().plusDays(1), "test@test.com", "123456")
        );

        assertThrows(IllegalArgumentException.class, () ->
                userService.registerClient("Juan", "Perez", IDType.DNI, "12345678", LocalDate.of(1990, 1, 1), "email-invalido", "123456")
        );
    }

    @Test
    @DisplayName("Unit: Registro falla si el correo ya se encuentra registrado")
    public void testRegisterClientDuplicateEmail() {
        String email = "existente@example.com";
        User existing = new User();
        existing.setUsername(email);
        existing.setDeleted(false);

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerClient("Carlos", "Lopez", IDType.DNI, "20111222", LocalDate.of(1992, 3, 10), email, "Password123")
        );
        assertTrue(ex.getMessage().contains("ya se encuentra registrado"));
    }

    @Test
    @DisplayName("Unit: Activación de cuenta con código de verificación válido")
    public void testVerifyAccountSuccess() {
        String email = "cliente@example.com";
        String validCode = "654321";

        User user = new User();
        user.setUsername(email);
        user.setVerificationCode(validCode);
        user.setVerified(false);

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = userService.verifyAccount(email, validCode);

        assertTrue(result, "La verificación debe retornar true con el código correcto");
        assertTrue(user.getVerified(), "El usuario debe quedar marcado como verificado");
        assertNull(user.getVerificationCode(), "El código debe eliminarse tras la activación");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Unit: Activación de cuenta falla con código incorrecto")
    public void testVerifyAccountInvalidCode() {
        String email = "cliente@example.com";
        User user = new User();
        user.setUsername(email);
        user.setVerificationCode("123456");
        user.setVerified(false);

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(user));

        boolean result = userService.verifyAccount(email, "999999");

        assertFalse(result, "La verificación debe fallar con código erróneo");
        assertFalse(user.getVerified());
        assertNotNull(user.getVerificationCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Reenvío de código de activación genera nuevo código")
    public void testResendVerificationCode() {
        String email = "cliente@example.com";
        User user = new User();
        user.setUsername(email);
        user.setVerificationCode("111111");

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        String newCode = userService.resendVerificationCode(email);

        assertNotNull(newCode);
        assertEquals(6, newCode.length());
        assertEquals(newCode, user.getVerificationCode());
        verify(emailService, times(1)).sendVerificationCodeEmail(eq(email), eq(newCode), anyString());
    }
}
