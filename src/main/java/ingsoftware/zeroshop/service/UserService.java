package ingsoftware.zeroshop.service;

import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para registrar un nuevo usuario en la base de datos
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

    // Método para actualizar el perfil de un usuario existente
    @Transactional
    public User updateProfile(UUID id, String firstName, String lastName, String email, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new IllegalArgumentException("Ese correo ya esta registrado.");
        }

        user.setFirst_name(requireText(firstName, "El nombre es obligatorio."));
        user.setLast_name(requireText(lastName, "El apellido es obligatorio."));
        user.setEmail(normalizedEmail);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    // Método para eliminar un usuario que no sea administrador
    @Transactional
    public void deleteNonAdmin(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("No se puede eliminar un administrador.");
        }
        userRepository.delete(user);
    }

    // Metodo para normalizar el correo electrónico y validar que no esté vacío
    private String normalizeEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        return normalizedEmail;
    }

    // Metodo para validar que un texto no esté vacío y devolverlo sin espacios al inicio y al final
    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}