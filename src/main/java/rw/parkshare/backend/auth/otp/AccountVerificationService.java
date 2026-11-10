package rw.parkshare.backend.auth.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.RwandaPhone;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Phone/email verification and password reset, all by one-time code.
 */
@Service
public class AccountVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AccountVerificationService.class);
    private static final String GENERIC_RESET_ERROR = "The code is invalid or has expired";

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final ParkShareProperties properties;
    private final rw.parkshare.backend.auth.token.RefreshTokenService refreshTokenService;

    public AccountVerificationService(UserRepository userRepository, OtpService otpService,
                                      PasswordEncoder passwordEncoder, ParkShareProperties properties,
                                      rw.parkshare.backend.auth.token.RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    /** Called after registration: phone code by SMS (if enabled) and email code by email (free). */
    @Transactional
    public void sendWelcomeCodes(User user) {
        try {
            if (properties.getMessaging().isVerifyPhoneOnRegister()) {
                otpService.send(user.getId(), user.getPhone(), OtpCode.Channel.SMS, OtpCode.Purpose.VERIFY_PHONE);
            }
            otpService.send(user.getId(), user.getEmail(), OtpCode.Channel.EMAIL, OtpCode.Purpose.VERIFY_EMAIL);
        } catch (ApiException exception) {
            log.warn("Could not send welcome codes to user {}: {}", user.getId(), exception.getMessage());
        }
    }

    @Transactional
    public void requestPhoneCode(Long userId) {
        User user = load(userId);
        if (user.getPhoneVerifiedAt() != null) {
            throw ApiException.conflict("Your phone number is already verified");
        }
        otpService.send(userId, user.getPhone(), OtpCode.Channel.SMS, OtpCode.Purpose.VERIFY_PHONE);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirmPhone(Long userId, String code) {
        User user = load(userId);
        otpService.verify(user.getPhone(), OtpCode.Purpose.VERIFY_PHONE, code);
        user.setPhoneVerifiedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void requestEmailCode(Long userId) {
        User user = load(userId);
        if (user.getEmailVerifiedAt() != null) {
            throw ApiException.conflict("Your email is already verified");
        }
        otpService.send(userId, user.getEmail(), OtpCode.Channel.EMAIL, OtpCode.Purpose.VERIFY_EMAIL);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirmEmail(Long userId, String code) {
        User user = load(userId);
        otpService.verify(user.getEmail(), OtpCode.Purpose.VERIFY_EMAIL, code);
        user.setEmailVerifiedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    /**
     * Sends a reset code to the phone or email given. Always behaves the same
     * whether or not an account exists, so it cannot be used to discover accounts.
     */
    @Transactional
    public void forgotPassword(String identifier) {
        Optional<Contact> contact = resolve(identifier);
        if (contact.isEmpty()) {
            return;
        }
        Contact c = contact.get();
        try {
            otpService.send(c.user().getId(), c.destination(), c.channel(), OtpCode.Purpose.RESET_PASSWORD);
        } catch (ApiException exception) {
            if (exception.getStatus().value() == 429) {
                throw exception;
            }
        }
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void resetPassword(String identifier, String code, String newPassword) {
        if (newPassword == null || newPassword.length() < 8
                || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*")) {
            throw ApiException.badRequest("New password needs at least 8 characters, including a letter and a number");
        }
        Contact c = resolve(identifier).orElseThrow(() -> ApiException.badRequest(GENERIC_RESET_ERROR));
        otpService.verify(c.destination(), OtpCode.Purpose.RESET_PASSWORD, code);

        User user = c.user();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAllForUser(user.getId()); // sign out every device
        // Receiving the code proves the user controls this phone/email.
        if (c.channel() == OtpCode.Channel.SMS && user.getPhoneVerifiedAt() == null) {
            user.setPhoneVerifiedAt(OffsetDateTime.now());
        }
        if (c.channel() == OtpCode.Channel.EMAIL && user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(OffsetDateTime.now());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    private Optional<Contact> resolve(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw ApiException.badRequest("Enter your phone number or email");
        }
        String value = identifier.trim();
        if (value.contains("@")) {
            String email = value.toLowerCase();
            return userRepository.findByEmail(email).map(user -> new Contact(user, email, OtpCode.Channel.EMAIL));
        }
        return RwandaPhone.normalize(value)
                .flatMap(phone -> userRepository.findByPhone(phone)
                        .map(user -> new Contact(user, phone, OtpCode.Channel.SMS)));
    }

    private User load(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private record Contact(User user, String destination, OtpCode.Channel channel) {
    }
}
