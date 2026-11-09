package rw.parkshare.backend.auth.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.service.MessageDispatcher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;

/**
 * Issues and checks one-time codes sent by SMS or email.
 * Codes expire, allow a limited number of attempts, and are rate-limited per
 * destination so the endpoints cannot be used to flood someone's phone.
 */
@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository repository;
    private final MessageDispatcher dispatcher;
    private final ParkShareProperties.Otp config;
    private final byte[] secret;

    public OtpService(OtpCodeRepository repository, MessageDispatcher dispatcher, ParkShareProperties properties,
                      @Value("${PARKSHARE_JWT_SECRET}") String secret) {
        this.repository = repository;
        this.dispatcher = dispatcher;
        this.config = properties.getMessaging().getOtp();
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void send(Long userId, String destination, OtpCode.Channel channel, OtpCode.Purpose purpose) {
        OffsetDateTime now = OffsetDateTime.now();
        repository.findFirstByDestinationAndPurposeOrderByCreatedAtDesc(destination, purpose)
                .filter(last -> last.getCreatedAt().isAfter(now.minusSeconds(config.getResendSeconds())))
                .ifPresent(last -> {
                    throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                            "Please wait " + config.getResendSeconds() + " seconds before requesting another code");
                });
        if (repository.countByDestinationAndPurposeAndCreatedAtAfter(destination, purpose, now.minusHours(1))
                >= config.getMaxPerHour()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many codes requested; try again later");
        }

        String code = generate();
        OtpCode otp = new OtpCode();
        otp.setUserId(userId);
        otp.setDestination(destination);
        otp.setChannel(channel);
        otp.setPurpose(purpose);
        otp.setCodeHash(hash(destination, purpose, code));
        otp.setExpiresAt(now.plusMinutes(config.getTtlMinutes()));
        otp.setCreatedAt(now);
        repository.save(otp);

        String text = "Your ParkShare " + describe(purpose) + " code is " + code + ". It expires in "
                + config.getTtlMinutes() + " minutes. Never share this code with anyone.";
        if (channel == OtpCode.Channel.SMS) {
            dispatcher.sms(destination, text);
        } else {
            dispatcher.email(destination, "Your ParkShare code: " + code, text);
        }
    }

    /**
     * Checks a code. Wrong attempts are counted even though an error is thrown,
     * so this commits on {@link ApiException}.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public void verify(String destination, OtpCode.Purpose purpose, String code) {
        OtpCode otp = repository.findFirstByDestinationAndPurposeOrderByCreatedAtDesc(destination, purpose)
                .filter(candidate -> candidate.getConsumedAt() == null)
                .filter(candidate -> candidate.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> ApiException.badRequest("The code is invalid or has expired; request a new one"));
        if (otp.getAttempts() >= config.getMaxAttempts()) {
            throw ApiException.badRequest("Too many wrong attempts; request a new code");
        }
        otp.setAttempts(otp.getAttempts() + 1);
        boolean matches = code != null && MessageDigest.isEqual(
                otp.getCodeHash().getBytes(StandardCharsets.UTF_8),
                hash(destination, purpose, code.trim()).getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            repository.save(otp);
            throw ApiException.badRequest("The code is incorrect");
        }
        otp.setConsumedAt(OffsetDateTime.now());
        repository.save(otp);
    }

    private String generate() {
        int bound = (int) Math.pow(10, config.getLength());
        return String.format("%0" + config.getLength() + "d", RANDOM.nextInt(bound));
    }

    private String hash(String destination, OtpCode.Purpose purpose, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] digest = mac.doFinal((destination + "|" + purpose + "|" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String describe(OtpCode.Purpose purpose) {
        return switch (purpose) {
            case VERIFY_PHONE -> "phone verification";
            case VERIFY_EMAIL -> "email verification";
            case RESET_PASSWORD -> "password reset";
        };
    }
}
