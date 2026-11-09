package rw.parkshare.backend.reservation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationQrCode;
import rw.parkshare.backend.reservation.repository.ReservationQrCodeRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues and verifies reservation QR tokens.
 *
 * Token format: PS1.{reservationId}.{qrId}.{signature}, where the signature is
 * HMAC-SHA256 over the ids with a server secret. Tokens cannot be forged or
 * guessed, and only their SHA-256 hash is stored. The app turns the token into
 * a QR image; the attendant's scanner sends it back to /api/check-ins/scan.
 */
@Service
@Transactional
public class QrTokenService {

    private static final String PREFIX = "PS1";

    private final ReservationQrCodeRepository qrRepository;
    private final byte[] secret;

    public QrTokenService(
            ReservationQrCodeRepository qrRepository,
            @Value("${PARKSHARE_QR_SECRET:${PARKSHARE_JWT_SECRET}}") String secret
    ) {
        this.qrRepository = qrRepository;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** Creates a fresh QR code for a confirmed reservation, replacing any previous one. */
    public String issue(Reservation reservation) {
        qrRepository.findByReservation_IdAndActiveTrue(reservation.getId()).forEach(old -> {
            old.setActive(false);
            qrRepository.save(old);
        });

        ReservationQrCode qr = new ReservationQrCode();
        qr.setReservation(reservation);
        qr.setTokenHash("pending-" + UUID.randomUUID());
        qr.setExpiresAt(reservation.getEndTime());
        qr.setActive(true);
        qr.setCreatedAt(OffsetDateTime.now());
        qr = qrRepository.saveAndFlush(qr);

        String token = build(reservation.getId(), qr.getId());
        qr.setTokenHash(sha256(token));
        qrRepository.save(qr);
        return token;
    }

    @Transactional(readOnly = true)
    public Optional<CurrentQr> current(Reservation reservation) {
        return qrRepository.findByReservation_IdAndActiveTrue(reservation.getId()).stream()
                .findFirst()
                .map(qr -> new CurrentQr(build(reservation.getId(), qr.getId()), qr.getExpiresAt(), qr.getUsedAt()));
    }

    /** Verifies a scanned token and returns its QR record, or fails with a clear reason. */
    public ReservationQrCode resolve(String token) {
        if (token == null || token.isBlank()) {
            throw ApiException.badRequest("QR token is required");
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            throw ApiException.badRequest("Not a ParkShare QR code");
        }
        long reservationId;
        long qrId;
        try {
            reservationId = Long.parseLong(parts[1]);
            qrId = Long.parseLong(parts[2]);
        } catch (NumberFormatException exception) {
            throw ApiException.badRequest("Not a ParkShare QR code");
        }
        String expected = build(reservationId, qrId);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                token.trim().getBytes(StandardCharsets.UTF_8))) {
            throw ApiException.badRequest("QR code signature is invalid");
        }
        ReservationQrCode qr = qrRepository.findByTokenHash(sha256(expected))
                .orElseThrow(() -> ApiException.notFound("QR code not found"));
        if (!qr.isActive()) {
            throw ApiException.conflict("This QR code is no longer active");
        }
        if (qr.getUsedAt() != null) {
            throw ApiException.conflict("This QR code has already been used");
        }
        if (qr.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw ApiException.conflict("This QR code has expired");
        }
        return qr;
    }

    public void markUsed(ReservationQrCode qr) {
        qr.setUsedAt(OffsetDateTime.now());
        qrRepository.save(qr);
    }

    public void deactivateAll(Reservation reservation) {
        qrRepository.findByReservation_IdAndActiveTrue(reservation.getId()).forEach(qr -> {
            qr.setActive(false);
            qrRepository.save(qr);
        });
    }

    private String build(long reservationId, long qrId) {
        String body = reservationId + "." + qrId;
        return PREFIX + "." + body + "." + sign(body);
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] digest = mac.doFinal(("qr:" + body).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to sign QR token", exception);
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record CurrentQr(String token, OffsetDateTime expiresAt, OffsetDateTime usedAt) {
    }
}
