package rw.parkshare.backend.notification.service;

import org.springframework.stereotype.Component;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.entity.Notification;
import rw.parkshare.backend.notification.repository.NotificationRepository;
import rw.parkshare.backend.user.entity.User;

import java.time.OffsetDateTime;

/**
 * Tells users what happened, choosing channels by cost:
 * - in-app: always (free);
 * - email: whenever the user has an email address (free on Brevo's tier);
 * - SMS: only for events listed in parkshare.messaging.sms-events
 *   (default: booking confirmed, booking cancelled), because each SMS costs money.
 */
@Component
public class Notifier {

    private final NotificationRepository notificationRepository;
    private final MessageDispatcher dispatcher;
    private final ParkShareProperties properties;

    public Notifier(NotificationRepository notificationRepository, MessageDispatcher dispatcher,
                    ParkShareProperties properties) {
        this.notificationRepository = notificationRepository;
        this.dispatcher = dispatcher;
        this.properties = properties;
    }

    /** In-app + email, plus SMS for important events. */
    public void notify(User user, String type, String title, String message, String referenceType, Long referenceId) {
        inApp(user, type, title, message, referenceType, referenceId);
        dispatcher.email(user.getEmail(), "ParkShare: " + title, emailBody(user, message));
        if (properties.getMessaging().getSmsEvents().contains(type)) {
            dispatcher.sms(user.getPhone(), "ParkShare: " + message);
        }
    }

    /** In-app + email, never SMS (for low-importance events). */
    public void inAppAndEmail(User user, String type, String title, String message, String referenceType, Long referenceId) {
        inApp(user, type, title, message, referenceType, referenceId);
        dispatcher.email(user.getEmail(), "ParkShare: " + title, emailBody(user, message));
    }

    /** In-app only (free). */
    public void inApp(User user, String type, String title, String message, String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setChannel("IN_APP");
        notification.setStatus("UNREAD");
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    private static String emailBody(User user, String message) {
        return "Muraho " + user.getFirstName() + ",\n\n"
                + message + "\n\n"
                + "Thank you for using ParkShare.\n"
                + "This is an automated message; please do not reply.";
    }
}
