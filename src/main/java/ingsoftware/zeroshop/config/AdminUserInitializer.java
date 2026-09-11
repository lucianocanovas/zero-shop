package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.users.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// CLASE DE PRUEBA
// Inicializa un usuario administrador en la base de datos al iniciar la aplicación para propósitos de prueba y desarrollo.
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para inicializar el usuario administrador en la base de datos si no existe
    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@gmail.com";
        String clientEmail = "client@gmail.com";

        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(clientEmail)) {
            return;
        }

        User admin = new User();
        admin.setFirst_name("Admin");
        admin.setLast_name("Sistema");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setCreated_at(LocalDateTime.now());

        User client = new User();
        client.setFirst_name("Cliente");
        client.setLast_name("Sistema");
        client.setEmail(clientEmail);
        client.setPassword(passwordEncoder.encode("client123"));
        client.setRole(Role.USER);
        client.setCreated_at(LocalDateTime.now());

        userRepository.save(admin);
        userRepository.save(client);
    }
}
