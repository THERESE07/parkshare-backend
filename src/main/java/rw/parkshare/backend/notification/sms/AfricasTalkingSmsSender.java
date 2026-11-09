package rw.parkshare.backend.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import rw.parkshare.backend.config.ParkShareProperties;

/**
 * Sends SMS through Africa's Talking. With username "sandbox" it uses their
 * free test environment (messages appear in the sandbox simulator, not on real phones).
 *
 * Enable with:
 *   PARKSHARE_MESSAGING_SMSPROVIDER=africastalking
 *   PARKSHARE_MESSAGING_AFRICASTALKING_USERNAME=sandbox (or your app username)
 *   PARKSHARE_MESSAGING_AFRICASTALKING_APIKEY=...
 */
@Component
@ConditionalOnProperty(prefix = "parkshare.messaging", name = "sms-provider", havingValue = "africastalking")
public class AfricasTalkingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(AfricasTalkingSmsSender.class);
    private static final String LIVE_URL = "https://api.africastalking.com/version1/messaging";
    private static final String SANDBOX_URL = "https://api.sandbox.africastalking.com/version1/messaging";

    private final ParkShareProperties.AfricasTalking config;
    private final RestClient client = RestClient.create();

    public AfricasTalkingSmsSender(ParkShareProperties properties) {
        this.config = properties.getMessaging().getAfricasTalking();
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new IllegalStateException("PARKSHARE_MESSAGING_AFRICASTALKING_APIKEY is required for Africa's Talking SMS");
        }
    }

    @Override
    public void send(String phone, String message) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", config.getUsername());
        form.add("to", phone);
        form.add("message", message);
        if (config.getSenderId() != null && !config.getSenderId().isBlank()) {
            form.add("from", config.getSenderId());
        }
        String url = "sandbox".equalsIgnoreCase(config.getUsername()) ? SANDBOX_URL : LIVE_URL;
        String response = client.post()
                .uri(url)
                .header("apiKey", config.getApiKey())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
        log.info("Africa's Talking SMS to {}: {}", phone, response);
    }
}
