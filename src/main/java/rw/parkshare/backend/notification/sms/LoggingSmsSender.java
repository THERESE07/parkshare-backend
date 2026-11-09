package rw.parkshare.backend.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default for development: prints SMS to the log instead of sending them. */
@Component
@ConditionalOnProperty(prefix = "parkshare.messaging", name = "sms-provider", havingValue = "log", matchIfMissing = true)
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phone, String message) {
        log.info("[SMS not sent - log mode] to={} message={}", phone, message);
    }
}
