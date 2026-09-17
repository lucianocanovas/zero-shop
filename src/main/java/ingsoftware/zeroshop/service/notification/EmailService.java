package ingsoftware.zeroshop.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Envia un correo electrónico simple de forma segura
    public void sendEmail(String to, String subject, String text) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
            }
        } catch (Exception e) {
            // Manejo de excepción o registro de log
        }
    }

}

