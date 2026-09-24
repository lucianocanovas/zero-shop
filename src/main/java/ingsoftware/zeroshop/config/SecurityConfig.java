package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configuración de la cadena de filtros de seguridad HTTP (SecurityFilterChain).
     * Aquí se definen las reglas de autorización para las rutas, la configuración del formulario
     * de inicio de sesión, el cierre de sesión, y la protección CSRF.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Reglas de autorización de solicitudes HTTP
            .authorizeHttpRequests(authorize -> authorize
                // Recursos estáticos públicos (CSS, JS, imágenes, webjars)
                .requestMatchers(
                    "/styles/**",
                    "/scripts/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Rutas públicas de navegación y catálogo
                .requestMatchers(
                    "/",
                    "/contact",
                    "/products/**",
                    "/categories/**",
                    "/offers"
                ).permitAll()

                // Rutas públicas de autenticación y registro
                .requestMatchers(
                    "/login",
                    "/register",
                    "/verify/**"
                ).permitAll()

                // Rutas exclusivas para administradores dentro del dashboard
                .requestMatchers(
                    "/dashboard/admin/**",
                    "/dashboard/users/**",
                    "/dashboard/offices/**",
                    "/dashboard/products/new"
                ).hasRole(Role.ADMIN.name())
                .requestMatchers(
                    org.springframework.http.HttpMethod.POST, "/dashboard/products", "/dashboard/products/**"
                ).hasRole(Role.ADMIN.name())
                .requestMatchers(
                    org.springframework.http.HttpMethod.PUT, "/dashboard/products/**"
                ).hasRole(Role.ADMIN.name())
                .requestMatchers(
                    org.springframework.http.HttpMethod.DELETE, "/dashboard/products/**"
                ).hasRole(Role.ADMIN.name())

                // Rutas exclusivas para empleados dentro del dashboard
                .requestMatchers("/dashboard/employee/**").hasRole(Role.EMPLOYEE.name())

                // Rutas comunes del Dashboard (Staff: ADMIN y EMPLOYEE)
                .requestMatchers("/dashboard/**", "/dashboard").hasAnyRole(
                    Role.ADMIN.name(),
                    Role.EMPLOYEE.name()
                )

                // Rutas para clientes y usuarios autenticados (perfil, pedidos, checkout)
                .requestMatchers("/profile/**", "/orders/**", "/checkout/**").hasAnyRole(
                    Role.CLIENT.name(),
                    Role.ADMIN.name(),
                    Role.EMPLOYEE.name()
                )

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )

            // 2. Configuración de Form Login (coincide con templates/login.html)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login") // Endpoint POST manejado por Spring Security
                .usernameParameter("username") // Nombre del input de usuario/email en login.html
                .passwordParameter("password") // Nombre del input de contraseña en login.html
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
                    boolean isEmployee = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.EMPLOYEE.name()));

                    if (isAdmin) {
                        response.sendRedirect("/");
                    } else if (isEmployee) {
                        response.sendRedirect("/dashboard/employee");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .failureUrl("/login?error=true") // Redirección tras fallo en credenciales
                .permitAll()
            )

            // 3. Configuración de Cierre de Sesión (Logout)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

            // 4. Configuración de "Recordarme" (Remember-me) - Requiere un bean UserDetailsService activo
            // .rememberMe(remember -> remember
            //     .key("zeroShopRememberMeSecretKey")
            //     .tokenValiditySeconds(7 * 24 * 60 * 60)
            //     .rememberMeParameter("remember-me")
            // );

        return http.build();
    }

    /**
     * Codificador de contraseñas recomendado (BCrypt).
     * Se utiliza para encriptar contraseñas al registrar usuarios y verificar credenciales en el login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager como un Bean de Spring.
     * Requerido por componentes como AuthController para autenticación programática.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

