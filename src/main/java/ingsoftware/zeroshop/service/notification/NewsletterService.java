package ingsoftware.zeroshop.service.notification;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.enums.Role;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de la programación y envío periódico (cada 10 días)
 * de boletines de ofertas a los clientes registrados, conforme al requerimiento del integrador.
 */
@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);

    // 10 días expresados en milisegundos: 10 * 24 * 60 * 60 * 1000 = 864.000.000 ms
    public static final long TEN_DAYS_IN_MS = 864_000_000L;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NewsletterService(ProductRepository productRepository,
                             UserRepository userRepository,
                             EmailService emailService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Tarea programada: se ejecuta automáticamente cada 10 días.
     * Envía un correo con HTML embebido mostrando los productos en oferta.
     */
    @Scheduled(fixedRate = TEN_DAYS_IN_MS, initialDelay = 60_000L)
    @Transactional(readOnly = true)
    public int sendScheduledNewsletter() {
        log.info("Iniciando despacho periódico de newsletter de ofertas a clientes...");
        return sendPromotionalNewsletter();
    }

    /**
     * Compila y despacha la newsletter de productos en oferta a todos los clientes activos.
     * Retorna la cantidad de correos enviados.
     */
    @Transactional(readOnly = true)
    public int sendPromotionalNewsletter() {
        List<Product> saleProducts = productRepository.findByOnSaleTrueAndDeletedFalse();
        if (saleProducts.isEmpty()) {
            log.info("No hay productos en oferta actualmente. Newsletter omitida.");
            return 0;
        }

        List<User> clients = userRepository.findAllByDeletedFalse().stream()
                .filter(u -> u.getRole() == Role.CLIENT && u.getUsername() != null && !u.getUsername().isBlank())
                .toList();

        if (clients.isEmpty()) {
            log.info("No hay clientes activos registrados para recibir el newsletter.");
            return 0;
        }

        String htmlContent = buildNewsletterHtml(saleProducts);
        String subject = "🔥 ¡Ofertas Exclusivas de la Semana en Zero Shop Mendoza!";

        int sentCount = 0;
        for (User client : clients) {
            try {
                emailService.sendHtmlEmail(client.getUsername(), subject, htmlContent);
                sentCount++;
            } catch (Exception e) {
                log.error("Error al enviar newsletter a {}: {}", client.getUsername(), e.getMessage());
            }
        }

        log.info("Newsletter de ofertas despachada exitosamente a {} clientes.", sentCount);
        return sentCount;
    }

    /**
     * Genera el HTML embebido para la presentación visual de productos en oferta.
     */
    public String buildNewsletterHtml(List<Product> products) {
        StringBuilder itemsHtml = new StringBuilder();
        for (Product p : products) {
            String name = p.getName() != null ? p.getName() : "Artículo Deportivo";
            String desc = p.getDescription() != null ? p.getDescription() : "Diseño exclusivo Zero Shop";
            String price = p.getCurrentPrice() != null ? "$" + p.getCurrentPrice() : "Consultar precio";
            String size = p.getSize() != null ? p.getSize().name() : "-";
            String imageUrl = (p.getImageUrl() != null && !p.getImageUrl().isBlank())
                    ? p.getImageUrl()
                    : "https://placehold.co/300x200/0d6efd/ffffff?text=ZERO+OFERTA";

            itemsHtml.append("""
                <div style="background:#ffffff; border:1px solid #e2e8f0; border-radius:12px; overflow:hidden; margin-bottom:20px; box-shadow:0 2px 4px rgba(0,0,0,0.05);">
                    <img src="%s" alt="%s" style="width:100%%; max-height:220px; object-fit:cover; display:block;" />
                    <div style="padding:16px;">
                        <span style="background:#ef4444; color:#ffffff; font-size:11px; font-weight:bold; padding:3px 8px; border-radius:20px; text-transform:uppercase;">EN OFERTA</span>
                        <h3 style="margin:10px 0 6px 0; font-size:18px; color:#1e293b;">%s</h3>
                        <p style="margin:0 0 10px 0; font-size:13px; color:#64748b;">%s (Talle: %s)</p>
                        <div style="font-size:20px; font-weight:bold; color:#0f172a;">%s</div>
                    </div>
                </div>
            """.formatted(imageUrl, name, name, desc, size, price));
        }

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color:#f1f5f9; margin:0; padding:20px; }
                    .wrapper { max-width:600px; margin:0 auto; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); }
                    .header { background:#0f172a; color:#ffffff; padding:32px 24px; text-align:center; }
                    .header h1 { margin:0; font-size:26px; letter-spacing:1px; }
                    .header p { margin:8px 0 0 0; color:#94a3b8; font-size:14px; }
                    .content { padding:24px; background:#f8fafc; }
                    .footer { text-align:center; padding:24px; color:#64748b; font-size:12px; background:#ffffff; border-top:1px solid #e2e8f0; }
                    .cta-btn { display:inline-block; background:#2563eb; color:#ffffff !important; text-decoration:none; font-weight:bold; padding:12px 28px; border-radius:8px; margin-top:16px; }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <div class="header">
                        <h1>ZERO SHOP</h1>
                        <p>Moda y Equipamiento Deportivo Unisex • Mendoza</p>
                    </div>
                    <div class="content">
                        <p style="font-size:15px; color:#334155; margin-bottom:20px;">
                            ¡Hola! Te compartimos nuestra selección especial de productos con descuentos imperdibles para renovar tu equipamiento deportivo.
                        </p>
                        %s
                        <div style="text-align:center; margin-top:24px;">
                            <a href="http://localhost:8080/offers" class="cta-btn">Ver Todas las Ofertas en la Tienda</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 Zero Shop Mendoza. Recibes este correo porque tienes una cuenta activa en nuestra plataforma.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(itemsHtml.toString());
    }
}
