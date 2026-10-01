package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ingsoftware.zeroshop.enums.Gender;
import java.time.LocalDate;

@SpringBootTest
@Transactional
public class ProfilePromotionsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final String testEmail = "cliente.promo@test.com";

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (userRepository.findByUsernameIgnoreCase(testEmail).isEmpty()) {
            Client client = new Client();
            client.setFirstName("Cliente");
            client.setLastName("Promo");
            client.setIdType(IDType.DNI);
            client.setIdNumber("40999888");
            client.setDateOfBirth(LocalDate.of(1995, 5, 20));
            client.setGender(Gender.MALE);
            client.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 6));
            clientRepository.save(client);

            User user = new User();
            user.setUsername(testEmail);
            user.setPassword(passwordEncoder.encode("Password123!"));
            user.setRole(Role.CLIENT);
            user.setPerson(client);
            user.setEmailPromotionsEnabled(true);
            userRepository.save(user);
        }
    }

    @Test
    @DisplayName("Perfil: GET /profile incluye modelo con atributo emailPromotionsEnabled")
    @WithMockUser(username = "cliente.promo@test.com", roles = {"CLIENT"})
    public void testGetProfilePageContainsPromotions() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("promotionsSlider")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Enviar email de prueba")));
    }

    @Test
    @DisplayName("Perfil: POST /profile/promotions/toggle desactiva las promociones por email")
    @WithMockUser(username = "cliente.promo@test.com", roles = {"CLIENT"})
    public void testTogglePromotionsDisable() throws Exception {
        mockMvc.perform(post("/profile/promotions/toggle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.enabled").value(false));

        User user = userRepository.findByUsernameIgnoreCase(testEmail).orElseThrow();
        assertFalse(user.getEmailPromotionsEnabled(), "Las promociones deben estar desactivadas en la BBDD");
    }

    @Test
    @DisplayName("Perfil: POST /profile/promotions/toggle activa las promociones por email")
    @WithMockUser(username = "cliente.promo@test.com", roles = {"CLIENT"})
    public void testTogglePromotionsEnable() throws Exception {
        // Primero desactivar
        User user = userRepository.findByUsernameIgnoreCase(testEmail).orElseThrow();
        user.setEmailPromotionsEnabled(false);
        userRepository.save(user);

        // Luego activar vía endpoint
        mockMvc.perform(post("/profile/promotions/toggle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.enabled").value(true));

        User updatedUser = userRepository.findByUsernameIgnoreCase(testEmail).orElseThrow();
        assertTrue(updatedUser.getEmailPromotionsEnabled(), "Las promociones deben estar activadas en la BBDD");
    }

    @Test
    @DisplayName("Perfil: POST /profile/promotions/test-email envía email de prueba exitosamente (AJAX)")
    @WithMockUser(username = "cliente.promo@test.com", roles = {"CLIENT"})
    public void testSendTestPromotionalEmailAjax() throws Exception {
        mockMvc.perform(post("/profile/promotions/test-email")
                        .with(csrf())
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Perfil: POST /profile/promotions/test-email redirecciona con flash message en envío tradicional")
    @WithMockUser(username = "cliente.promo@test.com", roles = {"CLIENT"})
    public void testSendTestPromotionalEmailFormSubmit() throws Exception {
        mockMvc.perform(post("/profile/promotions/test-email")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));
    }
}
