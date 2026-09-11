package ingsoftware.zeroshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // Envia un correo electrónico simple de forma segura
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            // log.info("Correo simple enviado exitosamente a {}", to);
        } catch (Exception e) {
            // log.error("No se pudo enviar el correo a {}. Causa: {}", to, e.getMessage());
        }
    }
}
