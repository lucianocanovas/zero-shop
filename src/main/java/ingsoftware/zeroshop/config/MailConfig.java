package ingsoftware.zeroshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${MAIL_USERNAME:${spring.mail.username:}}")
    private String username;

    @Value("${MAIL_PASSWORD:${spring.mail.password:}}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        String cleanUsername = (username != null) ? username.trim() : "";
        String cleanPassword = (password != null) ? password.replace(" ", "").trim() : "";

        if (!cleanUsername.isBlank()) {
            mailSender.setUsername(cleanUsername);
        }
        if (!cleanPassword.isBlank()) {
            mailSender.setPassword(cleanPassword);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "false");

        if (!cleanUsername.isBlank() && !cleanPassword.isBlank()) {
            log.info("JavaMailSender configurado exitosamente para la cuenta SMTP: {}", cleanUsername);
        } else {
            log.warn("JavaMailSender iniciado sin credenciales SMTP completas. Los envíos operarán en modo simulador.");
        }

        return mailSender;
    }
}
