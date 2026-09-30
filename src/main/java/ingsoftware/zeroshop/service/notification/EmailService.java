package ingsoftware.zeroshop.service.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Servicio centralizado dedicado al envío y gestión de correos electrónicos del sistema Zero Shop.
 * Soporta envíos asíncronos en texto plano y HTML enriquecido con plantillas estéticas de la marca.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${MAIL_USERNAME:${spring.mail.username:}}")
    private String mailUsername;

    @Value("${MAIL_PASSWORD:${spring.mail.password:}}")
    private String mailPassword;

    @Value("${app.mail.sender-name:Zero Shop Mendoza}")
    private String senderName;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo electrónico simple de texto plano de manera asíncrona.
     */
    @Async
    public void sendEmail(String to, String subject, String text) {
        if (!isMailSenderAvailable()) {
            log.warn("[MODO SIMULADOR] Correo NO enviado a la red porque no hay credenciales SMTP configuradas en .env (MAIL_USERNAME / MAIL_PASSWORD).\n"
                    + "Para: {} | Asunto: {}\n{}", to, subject, text);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromAddress());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Correo de texto enviado exitosamente vía SMTP a: {}", to);
        } catch (Exception e) {
            log.error("Fallo de transporte SMTP al enviar correo de texto a {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Envía un correo electrónico con formato HTML enriquecido de manera asíncrona.
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!isMailSenderAvailable()) {
            log.warn("[MODO SIMULADOR] Correo HTML NO enviado a la red porque no hay credenciales SMTP configuradas en .env (MAIL_USERNAME / MAIL_PASSWORD).\n"
                    + "Para: {} | Asunto: {} | Longitud HTML: {} caracteres",
                    to, subject, htmlContent != null ? htmlContent.length() : 0);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(getFromAddress(), senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Correo HTML enviado exitosamente vía SMTP a: {}", to);
        } catch (MessagingException e) {
            log.error("Error al componer correo HTML para {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Fallo de transporte SMTP al enviar correo HTML a {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Envía un correo de bienvenida con diseño corporativo al registrarse una nueva cuenta de cliente.
     */
    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        String displayName = (firstName != null && !firstName.isBlank()) ? firstName : "Cliente";
        String subject = "¡Bienvenido a Zero Shop Mendoza!";
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 20px; color: #212529; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                        .header { background-color: #0d6efd; color: #ffffff; padding: 30px 20px; text-align: center; }
                        .content { padding: 30px; line-height: 1.6; }
                        .btn { display: inline-block; background-color: #0d6efd; color: #ffffff !important; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 20px; }
                        .footer { background-color: #f1f3f5; padding: 20px; text-align: center; font-size: 12px; color: #6c757d; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 style="margin:0; font-size: 24px;">Zero Shop Mendoza</h1>
                        </div>
                        <div class="content">
                            <h2>¡Hola, %s!</h2>
                            <p>Te damos una cálida bienvenida a <strong>Zero Shop</strong>. Tu cuenta ha sido creada exitosamente.</p>
                            <p>A partir de ahora podrás explorar nuestro catálogo exclusivo de indumentaria y calzado, guardar tus productos favoritos y comprar de forma rápida y segura.</p>
                            <div style="text-align: center;">
                                <a href="http://localhost:8080/shop" class="btn">Explorar la Tienda</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Zero Shop Mendoza. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(displayName);

        sendHtmlEmail(to, subject, html);
    }

    /**
     * Envía un correo con el comprobante y detalle de compra de un pedido.
     */
    @Async
    public void sendOrderReceiptEmail(String to,
                                      String customerName,
                                      UUID orderId,
                                      BigDecimal totalAmount,
                                      String orderStatus,
                                      String shippingAddress,
                                      List<String> items) {
        String subject = "Confirmación de Compra - Zero Shop #" + (orderId != null ? orderId.toString().substring(0, 8).toUpperCase() : "");
        StringBuilder itemsListHtml = new StringBuilder();
        if (items != null) {
            for (String item : items) {
                itemsListHtml.append("<li style='padding: 6px 0; border-bottom: 1px solid #e9ecef;'>").append(item).append("</li>");
            }
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 20px; color: #212529; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                        .header { background-color: #198754; color: #ffffff; padding: 25px; text-align: center; }
                        .content { padding: 30px; line-height: 1.6; }
                        .order-box { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px; padding: 15px; margin: 20px 0; }
                        .footer { background-color: #f1f3f5; padding: 20px; text-align: center; font-size: 12px; color: #6c757d; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 style="margin:0; font-size: 22px;">¡Gracias por tu compra!</h1>
                        </div>
                        <div class="content">
                            <p>Estimado/a <strong>%s</strong>,</p>
                            <p>Tu orden ha sido procesada correctamente en Zero Shop.</p>
                            
                            <div class="order-box">
                                <p style="margin:0 0 5px;"><strong>Número de Pedido:</strong> %s</p>
                                <p style="margin:0 0 5px;"><strong>Estado:</strong> %s</p>
                                <p style="margin:0 0 5px;"><strong>Total:</strong> $%s</p>
                                <p style="margin:0;"><strong>Dirección de Entrega:</strong> %s</p>
                            </div>

                            <h3 style="font-size: 16px; margin-top: 20px;">Productos:</h3>
                            <ul style="list-style: none; padding: 0; margin: 0;">
                                %s
                            </ul>

                            <p style="margin-top: 25px; font-size: 14px; color: #6c757d;">
                                Puedes dar seguimiento a tu paquete desde tu cuenta en la sección "Mis Compras".
                            </p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Zero Shop Mendoza. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                customerName != null ? customerName : "Cliente",
                orderId != null ? orderId.toString().substring(0, 8).toUpperCase() : "N/A",
                orderStatus != null ? orderStatus : "En proceso",
                totalAmount != null ? totalAmount.toString() : "0.00",
                shippingAddress != null ? shippingAddress : "Retiro en sucursal",
                itemsListHtml.toString()
        );

        sendHtmlEmail(to, subject, html);
    }

    /**
     * Envía un correo para recuperación de contraseña con enlace seguro.
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Recuperación de Contraseña - Zero Shop";
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                        .btn { display: inline-block; background-color: #dc3545; color: #ffffff !important; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Recuperación de Contraseña</h2>
                        <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en Zero Shop.</p>
                        <p>Haz clic en el siguiente botón para crear una nueva clave de acceso:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="btn">Restablecer Contraseña</a>
                        </div>
                        <p style="margin-top: 25px; font-size: 13px; color: #6c757d;">Si no realizaste esta solicitud, puedes ignorar este correo sin riesgos.</p>
                    </div>
                </body>
                </html>
                """.formatted(resetLink);

        sendHtmlEmail(to, subject, html);
    }

    /**
     * Envía un correo para la verificación de cuenta.
     */
    @Async
    public void sendAccountVerificationEmail(String to, String verificationLink) {
        String subject = "Verifica tu Cuenta - Zero Shop";
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                        .btn { display: inline-block; background-color: #0d6efd; color: #ffffff !important; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Verifica tu Correo Electrónico</h2>
                        <p>Por favor confirma tu dirección de correo electrónico para activar todas las funciones de tu cuenta en Zero Shop.</p>
                        <div style="text-align: center;">
                            <a href="%s" class="btn">Confirmar Correo</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationLink);

        sendHtmlEmail(to, subject, html);
    }

    /**
     * Envía un correo con código numérico de 6 dígitos y enlace para activar la cuenta (especificación integrador).
     */
    @Async
    public void sendVerificationCodeEmail(String to, String code, String verificationUrl) {
        String subject = "Código de Activación de Cuenta: " + code + " - Zero Shop";
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                        .code-box { background: #e0f2fe; color: #0369a1; font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 15px 25px; border-radius: 8px; display: inline-block; margin: 20px 0; }
                        .btn { display: inline-block; background-color: #0d6efd; color: #ffffff !important; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container" style="text-align: center;">
                        <h2 style="color: #0f172a;">¡Bienvenido a Zero Shop!</h2>
                        <p style="color: #475569;">Para completar tu registro y activar tu cuenta, ingresa el siguiente código de activación en la plataforma:</p>
                        <div class="code-box">%s</div>
                        <p style="color: #64748b; font-size: 14px;">También puedes ingresar a la página de activación haciendo clic en el siguiente botón:</p>
                        <div>
                            <a href="%s" class="btn">Activar mi Cuenta</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(code, verificationUrl != null ? verificationUrl : "http://localhost:8080/verify");

        sendHtmlEmail(to, subject, html);
    }

    public boolean isMailSenderAvailable() {
        return mailSender != null
                && mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
    }

    private String getFromAddress() {
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername.trim();
        }
        return "noreply@zeroshop.com";
    }
}
