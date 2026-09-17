package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component

public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args){
        // Crear usuarios predeterminados si no existen
        createAdminUser();
        createEmployeeUser();
        createClientUser();
    }

    private void createUserIfNotExists(String username, String rawPassword, Role role) {
        // Lógica para crear un usuario si no existe
        boolean exists = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).isPresent();
        if (!exists) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            // Crear y guardar el usuario en la base de datos
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setRole(role);
            userRepository.save(user);
        }
    }

    private void createAdminUser() {
        createUserIfNotExists("admin@gmail.com", "admin123", Role.ADMIN);
    }

    private void createEmployeeUser() {
        createUserIfNotExists("employee@gmail.com", "employee123", Role.EMPLOYEE);
    }

    private void createClientUser() {
        createUserIfNotExists("client@gmail.com", "client123", Role.CLIENT);
    }

}
