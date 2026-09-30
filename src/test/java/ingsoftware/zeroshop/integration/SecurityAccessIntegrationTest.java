package ingsoftware.zeroshop.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class SecurityAccessIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Seguridad: Rutas públicas (catálogo, ofertas, login, verify) accesibles para anónimos")
    @WithAnonymousUser
    public void testPublicEndpointsAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/offers"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/verify"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Seguridad: Rutas protegidas (perfil, órdenes) redirigen al login para anónimos")
    @WithAnonymousUser
    public void testProtectedEndpointsRedirectToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResponse().getRedirectedUrl().contains("/login")));

        mockMvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResponse().getRedirectedUrl().contains("/login")));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResponse().getRedirectedUrl().contains("/login")));
    }

    @Test
    @DisplayName("Seguridad: Cliente autenticado no puede acceder a /dashboard ni a /dashboard/admin")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENT"})
    public void testClientDeniedFromDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/admin/reports/sales"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: Empleado puede acceder a /dashboard pero no a /dashboard/admin")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLOYEE"})
    public void testEmployeeAllowedDashboardDeniedAdmin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/admin/reports/sales"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: Administrador tiene acceso total a /dashboard y /dashboard/admin")
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    public void testAdminAllowedEverywhere() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/admin/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/admin/reports/sales"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/admin/reports/stock"))
                .andExpect(status().isOk());
    }
}
