package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Clase de configuración de seguridad para la aplicación
@Configuration
public class SecurityConfig {

    // Bean para el codificador de contraseñas utilizando BCrypt
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para el servicio de detalles de usuario que carga los detalles del usuario a través de UserService
    @Bean
    UserDetailsService userDetailsService(UserService userService) {
        return username -> userService.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // Bean para el administrador de autenticación que se utiliza para autenticar a los usuarios
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Bean para la cadena de filtros de seguridad que define las reglas de autorización y autenticación
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize

                        // Rutas públicas que no requieren autenticación
                        .requestMatchers("/", "/products", "/login", "/register", "/register/**", "/logout", "/css/**", "/styles/**", "/assets/**", "/scripts/**").permitAll()
                        
                        // Rutas que requieren el rol de ADMIN para acceder
                        .requestMatchers("/admin/**", "/users", "/users/**").hasRole("ADMIN")

                        // Cualquier otra solicitud requiere autenticación (incluye /profile)
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                        .maximumSessions(1));
        return http.build();
    }
}