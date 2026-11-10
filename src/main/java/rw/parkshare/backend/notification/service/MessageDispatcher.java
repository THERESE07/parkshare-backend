package rw.parkshare.backend.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.email.EmailSender;
import rw.parkshare.backend.notification.sms.SmsSender;

/**
 * Sends SMS and email only after the database transaction commits (so nobody
 * gets "confirmed" for a booking that was rolled back), in the background
 * so a slow provider never delays the API. Failures are logged, never thrown.
 */
@Component
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final TaskExecutor executor;
    private final ParkShareProperties properties;

    public MessageDispatcher(SmsSender smsSender, EmailSender emailSender,
                             @Qualifier("applicationTaskExecutor") TaskExecutor executor,
                             ParkShareProperties properties) {
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.executor = executor;
        this.properties = properties;
    }

    public void sms(String phone, String text) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        afterCommit(() -> {
            try {
                smsSender.send(phone, text);
            } catch (RuntimeException exception) {
                log.warn("SMS to {} failed: {}", phone, exception.getMessage());
            }
        });
    }

    public void email(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            return;
        }
        afterCommit(() -> {
            try {
                emailSender.send(to, subject, body);
            } catch (RuntimeException exception) {
                log.warn("Email to {} failed: {}", to, exception.getMessage());
            }
        });
    }

    private void afterCommit(Runnable task) {
        Runnable run = properties.getMessaging().isAsync() ? () -> executor.execute(task) : task;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    run.run();
                }
            });
        } else {
            run.run();
        }
    }
}
