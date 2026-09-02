package ingsoftware.zeroshop.service;

import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String rawPassword, String confirmPassword) {
        if (rawPassword == null || confirmPassword == null || rawPassword.isBlank() || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("Debe completar ambas contraseñas.");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Ese correo ya esta registrado.");
        }
        User user = new User();
        user.setFirst_name(firstName.trim());
        user.setLast_name(lastName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        user.setCreated_at(LocalDateTime.now());
        return userRepository.save(user);
    }
}