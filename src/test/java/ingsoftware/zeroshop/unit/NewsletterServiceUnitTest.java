package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.service.notification.EmailService;
import ingsoftware.zeroshop.service.notification.NewsletterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsletterServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NewsletterService newsletterService;

    @Test
    @DisplayName("Unit: buildNewsletterHtml genera estructura HTML embebido con productos y ofertas")
    public void testBuildNewsletterHtml() {
        Product p = Product.builder()
                .id(UUID.randomUUID())
                .name("Short Running Pro")
                .description("Short liviano con tela transpirable")
                .size(Size.M)
                .currentPrice(new BigDecimal("15999.00"))
                .onSale(true)
                .build();

        String html = newsletterService.buildNewsletterHtml(List.of(p));

        assertNotNull(html);
        assertTrue(html.contains("ZERO SHOP"));
        assertTrue(html.contains("Short Running Pro"));
        assertTrue(html.contains("15999.00"));
        assertTrue(html.contains("Talle: M"));
        assertTrue(html.contains("EN OFERTA"));
    }

    @Test
    @DisplayName("Unit: sendPromotionalNewsletter no envía correos si no hay productos en oferta")
    public void testSendNewsletterNoOffers() {
        when(productRepository.findByOnSaleTrueAndDeletedFalse()).thenReturn(List.of());

        int count = newsletterService.sendPromotionalNewsletter();

        assertEquals(0, count);
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Unit: sendPromotionalNewsletter despacha correo a todos los clientes registrados")
    public void testSendNewsletterSuccess() {
        Product p = Product.builder()
                .name("Zapatillas Zero Air")
                .currentPrice(new BigDecimal("35000.00"))
                .onSale(true)
                .build();

        User client1 = new User();
        client1.setUsername("cliente1@gmail.com");
        client1.setRole(Role.CLIENT);

        User client2 = new User();
        client2.setUsername("cliente2@gmail.com");
        client2.setRole(Role.CLIENT);

        when(productRepository.findByOnSaleTrueAndDeletedFalse()).thenReturn(List.of(p));
        when(userRepository.findAllByDeletedFalse()).thenReturn(List.of(client1, client2));

        int sent = newsletterService.sendPromotionalNewsletter();

        assertEquals(2, sent);
        verify(emailService, times(1)).sendHtmlEmail(eq("cliente1@gmail.com"), anyString(), anyString());
        verify(emailService, times(1)).sendHtmlEmail(eq("cliente2@gmail.com"), anyString(), anyString());
    }
}
