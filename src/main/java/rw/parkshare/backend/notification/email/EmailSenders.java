package rw.parkshare.backend.notification.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import rw.parkshare.backend.config.ParkShareProperties;

/**
 * Email delivery. "log" (default) prints emails; "smtp" sends them through any
 * SMTP server. Brevo's free tier (300 emails/day) works with:
 *   PARKSHARE_MESSAGING_EMAILPROVIDER=smtp
 *   SPRING_MAIL_HOST=smtp-relay.brevo.com
 *   SPRING_MAIL_PORT=587
 *   SPRING_MAIL_USERNAME=<your Brevo SMTP login>
 *   SPRING_MAIL_PASSWORD=<your Brevo SMTP key>
 *   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
 */
@Configuration
public class EmailSenders {

    private static final Logger log = LoggerFactory.getLogger(EmailSenders.class);

    @Bean
    @ConditionalOnProperty(prefix = "parkshare.messaging", name = "email-provider", havingValue = "log", matchIfMissing = true)
    EmailSender loggingEmailSender() {
        return (to, subject, body) -> log.info("[Email not sent - log mode] to={} subject={}\n{}", to, subject, body);
    }

    @Bean
    @ConditionalOnProperty(prefix = "parkshare.messaging", name = "email-provider", havingValue = "smtp")
    EmailSender smtpEmailSender(ObjectProvider<JavaMailSender> mailSender, ParkShareProperties properties) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("Email provider is smtp but SPRING_MAIL_HOST is not set");
        }
        String from = properties.getMessaging().getEmailFrom();
        return (to, subject, body) -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        };
    }
}
