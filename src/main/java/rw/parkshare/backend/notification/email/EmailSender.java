package rw.parkshare.backend.notification.email;

/** Sends plain-text email. */
public interface EmailSender {

    void send(String to, String subject, String body);
}
