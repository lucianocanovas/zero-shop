package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.PendingRegistration;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.PendingRegistrationRepository;
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
import java.time.LocalDateTime;
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
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Unit: Registro de cliente exitoso genera código de activación de 6 dígitos en PendingRegistration")
    public void testRegisterClientGeneratesVerificationCode() {
        String email = "juan.perez@example.com";
        String rawPass = "Segura123";
        String encodedPass = "$2a$10$encodedHashDummy";

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.empty());
        when(personRepository.findByIdNumber("35123456")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPass)).thenReturn(encodedPass);
        when(pendingRegistrationRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(pendingRegistrationRepository.save(any(PendingRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PendingRegistration pending = userService.registerClient(
                "Juan", "Perez", IDType.DNI, "35123456",
                LocalDate.of(1995, 5, 20), email, rawPass);

        assertNotNull(pending);
        assertEquals(email, pending.getEmail());
        assertEquals(encodedPass, pending.getPassword());
        assertNotNull(pending.getVerificationCode(), "Debe autogenerarse un código de verificación");
        assertEquals(6, pending.getVerificationCode().length(), "El código debe ser de 6 dígitos");

        // NO se debe crear ni guardar User ni Client antes de la verificación
        verify(userRepository, never()).save(any());
        verify(clientRepository, never()).save(any());

        verify(emailService, times(1)).sendVerificationCodeEmail(eq(email), eq(pending.getVerificationCode()),
                anyString());
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerClient("Juan", "Perez", IDType.DNI, "35123456",
                        LocalDate.of(1995, 5, 20), email, "Password123"));

        assertTrue(ex.getMessage().contains("ya posee el límite máximo de 2 usuarios vinculados"));
        verify(pendingRegistrationRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Registro de cliente falla si las contraseñas o nombres están vacíos")
    public void testRegisterClientValidations() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerClient("", "Perez", IDType.DNI,
                "12345678", LocalDate.of(1990, 1, 1), "test@test.com", "123456"));

        assertThrows(IllegalArgumentException.class, () -> userService.registerClient("Juan", "Perez", IDType.DNI,
                "12345678", LocalDate.now().plusDays(1), "test@test.com", "123456"));

        assertThrows(IllegalArgumentException.class, () -> userService.registerClient("Juan", "Perez", IDType.DNI,
                "12345678", LocalDate.of(1990, 1, 1), "email-invalido", "123456"));
    }

    @Test
    @DisplayName("Unit: Registro falla si el correo ya se encuentra registrado")
    public void testRegisterClientDuplicateEmail() {
        String email = "existente@example.com";
        User existing = new User();
        existing.setUsername(email);
        existing.setDeleted(false);

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerClient("Carlos", "Lopez", IDType.DNI, "20111222", LocalDate.of(1992, 3, 10),
                        email, "Password123"));
        assertTrue(ex.getMessage().contains("ya se encuentra registrado"));
    }

    @Test
    @DisplayName("Unit: Activación de cuenta crea el usuario tras validar código en PendingRegistration")
    public void testVerifyAccountSuccessWithPendingRegistration() {
        String email = "cliente@example.com";
        String validCode = "654321";

        PendingRegistration pending = PendingRegistration.builder()
                .email(email)
                .firstName("Juan")
                .lastName("Perez")
                .idType(IDType.DNI)
                .idNumber("35123456")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .password("$2a$10$encodedPass")
                .verificationCode(validCode)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(pendingRegistrationRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(pending));
        when(personRepository.findByIdNumber("35123456")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = userService.verifyAccount(email, validCode);

        assertTrue(result, "La verificación debe retornar true con el código correcto");
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(pendingRegistrationRepository, times(1)).delete(pending);
        verify(emailService, times(1)).sendWelcomeEmail(eq(email), eq("Juan"));
    }

    @Test
    @DisplayName("Unit: Activación de cuenta falla con código incorrecto en PendingRegistration")
    public void testVerifyAccountInvalidCode() {
        String email = "cliente@example.com";
        PendingRegistration pending = PendingRegistration.builder()
                .email(email)
                .verificationCode("123456")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(pendingRegistrationRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(pending));

        boolean result = userService.verifyAccount(email, "999999");

        assertFalse(result, "La verificación debe fallar con código erróneo");
        verify(userRepository, never()).save(any());
        verify(pendingRegistrationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Unit: Activación de cuenta en usuario legado")
    public void testVerifyAccountLegacyUser() {
        String email = "legado@example.com";
        String validCode = "654321";

        User user = new User();
        user.setUsername(email);
        user.setVerificationCode(validCode);
        user.setVerified(false);

        when(pendingRegistrationRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = userService.verifyAccount(email, validCode);

        assertTrue(result);
        assertTrue(user.getVerified());
        assertNull(user.getVerificationCode());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Unit: Reenvío de código de activación genera nuevo código en PendingRegistration")
    public void testResendVerificationCode() {
        String email = "cliente@example.com";
        PendingRegistration pending = PendingRegistration.builder()
                .email(email)
                .verificationCode("111111")
                .build();

        when(pendingRegistrationRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(pending));
        when(pendingRegistrationRepository.save(any(PendingRegistration.class))).thenAnswer(i -> i.getArgument(0));

        String newCode = userService.resendVerificationCode(email);

        assertNotNull(newCode);
        assertEquals(6, newCode.length());
        assertEquals(newCode, pending.getVerificationCode());
        verify(emailService, times(1)).sendVerificationCodeEmail(eq(email), eq(newCode), anyString());
    }
}
