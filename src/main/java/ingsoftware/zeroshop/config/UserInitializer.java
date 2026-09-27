package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Order(2)
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(UserRepository userRepository,
                           ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Crear usuarios predeterminados si no existen
        createAdminUser();
        createEmployeeUser();
        createClientUser();
    }

    private User createUserIfNotExists(String username, String rawPassword, Role role) {
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        return userRepository.save(user);
    }

    private void createAdminUser() {
        createUserIfNotExists("admin@gmail.com", "admin123", Role.ADMIN);
    }

    private void createEmployeeUser() {
        createUserIfNotExists("employee@gmail.com", "employee123", Role.EMPLOYEE);
    }

    private void createClientUser() {
        User clientUser = createUserIfNotExists("client@gmail.com", "client123", Role.CLIENT);
        if (clientUser.getPerson() == null) {
            Client client = new Client();
            client.setFirstName("Juan");
            client.setLastName("Perez");
            client.setClientNumber("CLI-0001");
            client.setIdType(IDType.DNI);
            client.setIdNumber("40123456");
            client.setDateOfBirth(LocalDate.of(1995, 5, 20));
            client.setDeleted(false);
            client = clientRepository.save(client);

            clientUser.setPerson(client);
            userRepository.save(clientUser);
        }
    }

}