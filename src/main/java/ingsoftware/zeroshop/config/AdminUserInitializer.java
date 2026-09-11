package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.Persona;
import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.PersonaRepository;
import ingsoftware.zeroshop.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// CLASE DE PRUEBA
// Inicializa un usuario administrador en la base de datos al iniciar la aplicación para propósitos de prueba y desarrollo.
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PersonaRepository personaRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para inicializar el usuario administrador en la base de datos si no existe
    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@gmail.com";
        String clientEmail = "client@gmail.com";

        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            Persona adminPersona = new Persona();
            adminPersona.setNombre("Admin");
            adminPersona.setApellido("System");
            personaRepository.save(adminPersona);

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setPersona(adminPersona);
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmailIgnoreCase(clientEmail)) {
            Persona clientPersona = new Persona();
            clientPersona.setNombre("Client");
            clientPersona.setApellido("Test");
            personaRepository.save(clientPersona);

            User client = new User();
            client.setEmail(clientEmail);
            client.setPassword(passwordEncoder.encode("client123"));
            client.setRole(Role.USER);
            client.setPersona(clientPersona);
            userRepository.save(client);
        }
    }
}
