package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Mapea a un UserDetails de Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Debe estar hasheada con BCrypt en la base de datos
                .roles(user.getRole().name()) // Genera la autoridad 'ROLE_' + nombre del rol// O tu lógica de borrado lógico
                .build();
    }
}