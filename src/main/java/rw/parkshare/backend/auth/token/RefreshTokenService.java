package rw.parkshare.backend.auth.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Rotating refresh tokens.
 *
 * - Login creates a token "family". Each refresh revokes the presented token and
 *   issues a new one in the same family.
 * - If an already-rotated token is presented again (someone copied it), the whole
 *   family is revoked: both the thief and the user must log in again.
 * - Logout revokes the family; password change/reset revokes all of the user's tokens.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final ParkShareProperties properties;

    public RefreshTokenService(RefreshTokenRepository repository, ParkShareProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public String issueNewLogin(Long userId) {
        return issue(userId, UUID.randomUUID().toString());
    }

    /**
     * Exchanges a refresh token for a new one. Commits even when it fails,
     * so a detected reuse really revokes the family.
     *
     * @return the user id and the new raw token
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken token = find(rawToken);
        OffsetDateTime now = OffsetDateTime.now();
        if (token.getRevokedAt() != null) {
            if ("ROTATED".equals(token.getRevokedReason())) {
                repository.revokeFamily(token.getFamilyId(), "REUSE_DETECTED", now);
                log.warn("Refresh token reuse detected for user {}; session family revoked", token.getUserId());
            }
            throw ApiException.unauthorized("Your session has ended; please log in again");
        }
        if (token.getExpiresAt().isBefore(now)) {
            throw ApiException.unauthorized("Your session has expired; please log in again");
        }
        token.setRevokedAt(now);
        token.setRevokedReason("ROTATED");
        repository.save(token);
        return new Rotation(token.getUserId(), issue(token.getUserId(), token.getFamilyId()));
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), "LOGOUT", OffsetDateTime.now()));
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        repository.revokeAllForUser(userId, "PASSWORD_CHANGED", OffsetDateTime.now());
    }

    public long refreshTokenSeconds() {
        return properties.getSecurity().getRefreshTokenDays() * 24L * 3600L;
    }

    private String issue(Long userId, String familyId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setFamilyId(familyId);
        token.setTokenHash(hash(raw));
        token.setCreatedAt(OffsetDateTime.now());
        token.setExpiresAt(OffsetDateTime.now().plusSeconds(refreshTokenSeconds()));
        repository.save(token);
        return raw;
    }

    private RefreshToken find(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw ApiException.unauthorized("Refresh token is required");
        }
        return repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> ApiException.unauthorized("Your session has ended; please log in again"));
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record Rotation(Long userId, String refreshToken) {
    }
}
