package ingsoftware.zeroshop.service;

    
import ingsoftware.zeroshop.entity.Persona;
import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
<<<<<<< HEAD
import ingsoftware.zeroshop.repository.users.UserRepository;
=======
import ingsoftware.zeroshop.repository.PersonaRepository;
import ingsoftware.zeroshop.repository.UserRepository;
>>>>>>> 6241319d41bf5921ed8be544577cff8653c350fc
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Servicio para la gestión y lógica de negocio de usuarios
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor para inyectar dependencias del repositorio y codificador de contraseñas
    public UserService(UserRepository userRepository, PersonaRepository personaRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para obtener todos los usuarios registrados
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Método para buscar un usuario por su identificador único
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    // Método para buscar un usuario por su correo electrónico ignorando mayúsculas/minúsculas
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    // Método para obtener un usuario por correo o lanzar excepción si no existe
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe."));
    }

    // Método para obtener el primer nombre de un usuario por su correo
    @Transactional(readOnly = true)
    public String getUserFirstName(String email) {
        return findByEmail(email)
                .map(user -> user.getPersona() != null ? user.getPersona().getNombre() : email)
                .orElse(email);
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

        Persona persona = new Persona();
        persona.setNombre(firstName.trim());
        persona.setApellido(lastName.trim());
        personaRepository.save(persona);

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        user.setPersona(persona);
        
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

        Persona persona = user.getPersona();
        if (persona != null) {
            persona.setNombre(requireText(firstName, "El nombre es obligatorio."));
            persona.setApellido(requireText(lastName, "El apellido es obligatorio."));
            personaRepository.save(persona);
        } else {
            persona = new Persona();
            persona.setNombre(requireText(firstName, "El nombre es obligatorio."));
            persona.setApellido(requireText(lastName, "El apellido es obligatorio."));
            personaRepository.save(persona);
            user.setPersona(persona);
        }

        user.setEmail(normalizedEmail);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    // Método para actualizar el perfil de un usuario garantizando que no sea administrador
    @Transactional
    public User updateNonAdminProfile(UUID id, String firstName, String lastName, String email, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Solo se pueden modificar usuarios no administradores.");
        }
        return updateProfile(id, firstName, lastName, email, rawPassword);
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
