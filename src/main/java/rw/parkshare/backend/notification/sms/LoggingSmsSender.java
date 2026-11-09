package rw.parkshare.backend.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingSmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Bean
    @ConditionalOnMissingBean(SmsSender.class)
    public SmsSender smsSender() {
        return (phone, message) -> log.info("[SMS not sent - no gateway configured] to={} message={}", phone, message);
    }
}
