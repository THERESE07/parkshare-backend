package rw.parkshare.backend.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rw.parkshare.backend.notification.entity.Notification;
import rw.parkshare.backend.notification.repository.NotificationRepository;
import rw.parkshare.backend.notification.sms.SmsSender;
import rw.parkshare.backend.user.entity.User;

import java.time.OffsetDateTime;

/**
 * Used by business services to tell users what happened.
 * Always stores an in-app notification; optionally also sends an SMS.
 */
@Component
public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private final NotificationRepository notificationRepository;
    private final SmsSender smsSender;

    public Notifier(NotificationRepository notificationRepository, SmsSender smsSender) {
        this.notificationRepository = notificationRepository;
        this.smsSender = smsSender;
    }

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

    public void inAppAndSms(User user, String type, String title, String message, String referenceType, Long referenceId) {
        inApp(user, type, title, message, referenceType, referenceId);
        try {
            if (user.getPhone() != null) {
                smsSender.send(user.getPhone(), "ParkShare: " + message);
            }
        } catch (RuntimeException exception) {
            log.warn("SMS delivery failed for user {}: {}", user.getId(), exception.getMessage());
        }
    }
}
