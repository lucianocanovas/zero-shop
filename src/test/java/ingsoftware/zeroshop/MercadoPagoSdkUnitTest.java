package ingsoftware.zeroshop;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.service.transaction.MercadoPagoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MercadoPagoSdkUnitTest {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Test
    @DisplayName("Mercado Pago SDK: MercadoPagoConfig debe estar configurado con access token")
    public void testMercadoPagoConfigInitialization() {
        System.out.println("CONFIG ACCESS TOKEN: " + MercadoPagoConfig.getAccessToken());
        assertNotNull(MercadoPagoConfig.getAccessToken(), "El SDK de Mercado Pago debe tener un Access Token asignado");
        assertFalse(MercadoPagoConfig.getAccessToken().isBlank(), "El Access Token no debe estar vacío");
    }

    @Test
    @DisplayName("Mercado Pago SDK: Construcción de PreferenceRequest conforme a las especificaciones del SDK")
    public void testPreferenceRequestBuilder() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        // 1. Items
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(productId.toString())
                .title("Zapatillas Urbanas Zero Sport")
                .description("Calzado deportivo liviano")
                .quantity(2)
                .unitPrice(new BigDecimal("29999.00"))
                .currencyId("ARS")
                .build();

        assertEquals(productId.toString(), item.getId());
        assertEquals("Zapatillas Urbanas Zero Sport", item.getTitle());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("29999.00"), item.getUnitPrice());
        assertEquals("ARS", item.getCurrencyId());

        // 2. Payer
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name("Juan")
                .surname("Perez")
                .email("juan.perez@cliente.com")
                .build();

        assertEquals("Juan", payer.getName());
        assertEquals("Perez", payer.getSurname());
        assertEquals("juan.perez@cliente.com", payer.getEmail());

        // 3. BackUrls
        String baseUrl = "http://localhost:8080";
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/checkout/mp/success?orderId=" + orderId)
                .pending(baseUrl + "/checkout/mp/pending?orderId=" + orderId)
                .failure(baseUrl + "/checkout/mp/failure?orderId=" + orderId)
                .build();

        assertTrue(backUrls.getSuccess().contains("/checkout/mp/success"));
        assertTrue(backUrls.getPending().contains("/checkout/mp/pending"));
        assertTrue(backUrls.getFailure().contains("/checkout/mp/failure"));

        // 4. PreferenceRequest
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(orderId.toString())
                .build();

        assertNotNull(preferenceRequest);
        assertEquals(1, preferenceRequest.getItems().size());
        assertEquals("approved", preferenceRequest.getAutoReturn());
        assertEquals(orderId.toString(), preferenceRequest.getExternalReference());
    }

    @Test
    @DisplayName("Mercado Pago SDK: Servicio createPreference debe procesar una orden y retornar URL de checkout")
    public void testCreatePreferenceWithService() {
        Client client = new Client();
        client.setFirstName("Mariano");
        client.setLastName("Gomez");

        SaleOrder order = SaleOrder.builder()
                .id(UUID.randomUUID())
                .client(client)
                .totalAmount(new BigDecimal("49980.00"))
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Remera Entrenamiento Pro")
                .description("Remera transpirable")
                .build();

        OrderDetail detail = OrderDetail.builder()
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("24990.00"))
                .total(new BigDecimal("49980.00"))
                .build();

        // Ejecutar creación de preferencia a través de MercadoPagoService
        String redirectUrl = mercadoPagoService.createPreference(order, List.of(detail));

        assertNotNull(redirectUrl, "La URL de redirección no debe ser nula");
        assertTrue(redirectUrl.startsWith("http"), "La URL debe ser un enlace válido HTTP/HTTPS");
        // Verifica que la URL contenga la referencia a Mercado Pago o al flujo de retorno
        assertTrue(redirectUrl.contains("mercadopago.com") || redirectUrl.contains("/checkout/mp/success"),
                "La URL debe redirigir a Mercado Pago o al endpoint de éxito");
    }
}
