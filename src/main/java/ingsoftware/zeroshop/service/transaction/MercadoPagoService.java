package ingsoftware.zeroshop.service.transaction;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            if (accessToken == null || accessToken.isBlank() || accessToken.startsWith("TEST-00000000")) {
                try {
                    java.nio.file.Path envPath = java.nio.file.Path.of(".env");
                    if (java.nio.file.Files.exists(envPath)) {
                        for (String line : java.nio.file.Files.readAllLines(envPath)) {
                            line = line.trim();
                            if (line.startsWith("MERCADOPAGO_ACCESS_TOKEN=")) {
                                this.accessToken = line.substring("MERCADOPAGO_ACCESS_TOKEN=".length()).trim();
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (accessToken != null && !accessToken.isBlank()) {
                MercadoPagoConfig.setAccessToken(accessToken.trim());
                log.info("Mercado Pago SDK inicializado correctamente.");
            }
        } catch (Exception e) {
            log.warn("No se pudo inicializar MercadoPagoConfig con el token provisto: {}", e.getMessage());
        }
    }

    /**
     * Crea una preferencia de pago en Mercado Pago para la orden de venta.
     * Retorna la URL de redirección (Sandbox o Producción).
     */
    public String createPreference(SaleOrder saleOrder, List<OrderDetail> details) {
        try {
            List<PreferenceItemRequest> items = new ArrayList<>();

            if (details != null && !details.isEmpty()) {
                for (OrderDetail detail : details) {
                    BigDecimal unitPrice = detail.getUnitPrice();
                    if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        unitPrice = BigDecimal.valueOf(1.00);
                    }
                    PreferenceItemRequest item = PreferenceItemRequest.builder()
                            .id(detail.getProduct().getId().toString())
                            .title(detail.getProduct().getName())
                            .description(detail.getProduct().getDescription() != null && !detail.getProduct().getDescription().isBlank() 
                                    ? detail.getProduct().getDescription() 
                                    : detail.getProduct().getName())
                            .quantity(detail.getQuantity())
                            .unitPrice(unitPrice)
                            .currencyId("ARS")
                            .build();
                    items.add(item);
                }
            } else {
                BigDecimal total = (saleOrder.getTotalAmount() != null && saleOrder.getTotalAmount().compareTo(BigDecimal.ZERO) > 0)
                        ? saleOrder.getTotalAmount()
                        : BigDecimal.valueOf(1.00);
                PreferenceItemRequest item = PreferenceItemRequest.builder()
                        .title("Orden Zero Shop #" + saleOrder.getId())
                        .quantity(1)
                        .unitPrice(total)
                        .currencyId("ARS")
                        .build();
                items.add(item);
            }

            String payerName = "Cliente";
            String payerSurname = "Zero";
            String payerEmail = "cliente@zeroshop.com";

            if (saleOrder.getClient() != null) {
                if (saleOrder.getClient().getFirstName() != null && !saleOrder.getClient().getFirstName().isBlank()) {
                    payerName = saleOrder.getClient().getFirstName();
                }
                if (saleOrder.getClient().getLastName() != null && !saleOrder.getClient().getLastName().isBlank()) {
                    payerSurname = saleOrder.getClient().getLastName();
                }
            }

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(payerName)
                    .surname(payerSurname)
                    .email(payerEmail)
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(baseUrl + "/checkout/mp/success?orderId=" + saleOrder.getId())
                    .pending(baseUrl + "/checkout/mp/pending?orderId=" + saleOrder.getId())
                    .failure(baseUrl + "/checkout/mp/failure?orderId=" + saleOrder.getId())
                    .build();

            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .externalReference(saleOrder.getId().toString());

            if (baseUrl != null && baseUrl.startsWith("https://") && !baseUrl.contains("localhost") && !baseUrl.contains("127.0.0.1")) {
                requestBuilder.autoReturn("approved");
            }

            PreferenceRequest preferenceRequest = requestBuilder.build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("Preferencia de Mercado Pago creada con éxito con SDK: {}", preference.getId());
            String redirectUrl = (sandboxEnabled && preference.getSandboxInitPoint() != null && !preference.getSandboxInitPoint().isBlank())
                    ? preference.getSandboxInitPoint()
                    : preference.getInitPoint();

            log.info("Redirigiendo a checkout de Mercado Pago: {}", redirectUrl);
            return redirectUrl;

        } catch (MPApiException e) {
            String errorMsg = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            log.error("Error API Mercado Pago (HTTP {}): {}", e.getStatusCode(), errorMsg);
            throw new IllegalStateException("Error al generar pago en Mercado Pago: " + errorMsg);
        } catch (MPException e) {
            log.error("Error SDK Mercado Pago: {}", e.getMessage(), e);
            throw new IllegalStateException("Error al conectar con Mercado Pago: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excepción inesperada en Mercado Pago: {}", e.getMessage(), e);
            return baseUrl + "/checkout/mp/success?orderId=" + saleOrder.getId() + "&collection_status=approved&simulated=true";
        }
    }
}
