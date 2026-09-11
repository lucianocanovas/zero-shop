package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.entity.User;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.service.EmailService;
import ingsoftware.zeroshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerSeparationTest {

    @Test
    void authAndUserControllersAreSeparated() {
        assertNotNull(AuthController.class);
        assertNotNull(UserController.class);
        assertNotNull(HomeController.class);
        assertNotNull(AdminController.class);
    }

    @Test
    void controllersDoNotDependOnRepositories() {
        List<Class<?>> controllers = List.of(
                AuthController.class,
                UserController.class,
                HomeController.class,
                AdminController.class
        );

        for (Class<?> controller : controllers) {
            for (Field field : controller.getDeclaredFields()) {
                assertFalse(field.getType().getName().startsWith("ingsoftware.zeroshop.repository"),
                        controller.getSimpleName() + " no debe declarar campos de tipo repositorio: " + field.getName());
            }

            for (Constructor<?> constructor : controller.getConstructors()) {
                for (Class<?> paramType : constructor.getParameterTypes()) {
                    assertFalse(paramType.getName().startsWith("ingsoftware.zeroshop.repository"),
                            controller.getSimpleName() + " no debe inyectar repositorios en el constructor: " + paramType.getSimpleName());
                }
            }
        }
    }

    @Test
    void userController_listUsers_returnsAdminUsersView() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@shop.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Model model = new ConcurrentModel();

        when(userService.findAll()).thenReturn(List.of(new User()));
        when(userService.getUserFirstName("admin@shop.com")).thenReturn("Admin");

        String view = userController.listUsers(adminAuth, model);

        assertEquals("admin/users", view);
        assertTrue(model.containsAttribute("users"));
        assertTrue(model.containsAttribute("isAdmin"));
        verify(userService).findAll();
    }

    @Test
    void userController_viewProfile_returnsUserView() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);

        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "user@shop.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Model model = new ConcurrentModel();
        User user = new User();
        user.setEmail("user@shop.com");
        user.setFirst_name("User");
        user.setRole(Role.USER);

        when(userService.getByEmail("user@shop.com")).thenReturn(user);

        String view = userController.viewProfile(userAuth, model);

        assertEquals("user", view);
        assertEquals(user, model.getAttribute("profileUser"));
        assertEquals("/profile", model.getAttribute("formAction"));
        verify(userService).getByEmail("user@shop.com");
    }

    @Test
    void userController_deleteUser_callsService() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);

        UUID id = UUID.randomUUID();
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@shop.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = userController.deleteUser(id, adminAuth, redirectAttributes);

        assertEquals("redirect:/users", view);
        verify(userService).deleteNonAdmin(id);
    }

    @Test
    void authController_endpointsReturnExpectedViews() {
        UserService userService = mock(UserService.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        EmailService emailService = mock(EmailService.class);

        AuthController authController = new AuthController(userService, authenticationManager, emailService);

        assertEquals("login", authController.login());

        Model model = new ConcurrentModel();
        assertEquals("register", authController.registerForm(model));
        assertTrue(model.containsAttribute("registration"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        assertEquals("redirect:/login?logout", authController.logout(request));
        verify(session).invalidate();
    }
}
