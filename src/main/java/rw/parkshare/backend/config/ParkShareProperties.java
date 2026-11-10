package rw.parkshare.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.ZoneId;

/**
 * Business settings for ParkShare. Every value has a Rwanda-appropriate
 * default and can be overridden with environment variables, e.g.
 * PARKSHARE_PRICING_DEFAULTHOURLYRATE=400.
 *
 * All money values are in Rwandan francs (RWF), which has no minor unit
 * in practice, so amounts are kept as whole francs.
 */
@Data
@ConfigurationProperties(prefix = "parkshare")
public class ParkShareProperties {

    /** Rwanda uses Central Africa Time (UTC+2, no daylight saving). */
    private String timezone = "Africa/Kigali";

    private Pricing pricing = new Pricing();
    private Tax tax = new Tax();
    private Reservation reservation = new Reservation();
    private Payments payments = new Payments();
    private Security security = new Security();

    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    @Data
    public static class Pricing {
        /** Used when an operator has not configured an HOURLY rule. Kept affordable for Kigali. */
        private BigDecimal defaultHourlyRate = new BigDecimal("300");
        /** Used when an operator has not configured a DAILY rule: no one pays more than this per 24h. */
        private BigDecimal defaultDailyCap = new BigDecimal("3000");
        /** Smallest amount charged for any reservation. */
        private BigDecimal minimumCharge = new BigDecimal("200");
        /** Time is billed in blocks of this size (each started block is charged). */
        private int billingIncrementMinutes = 30;
        /** Amounts are rounded to the nearest multiple of this (RWF has no cents). */
        private BigDecimal roundingStep = new BigDecimal("10");
        /** Affordability ceiling: operators cannot set an hourly rate above this. */
        private BigDecimal maxHourlyRate = new BigDecimal("2000");
        /** Affordability ceiling: operators cannot set a daily rate above this. */
        private BigDecimal maxDailyRate = new BigDecimal("20000");
        /** Platform commission taken from the operator's share (not added on top for the driver). */
        private BigDecimal platformCommissionRate = new BigDecimal("0.10");
        /** Minutes after the reservation end before overstay is charged. */
        private int overstayGraceMinutes = 10;
    }

    @Data
    public static class Tax {
        /** Rwanda standard VAT rate (RRA): 18%. */
        private BigDecimal vatRate = new BigDecimal("0.18");
        /** Rwandan consumer prices are normally quoted VAT-inclusive. */
        private boolean pricesIncludeVat = true;
    }

    @Data
    public static class Reservation {
        private int minDurationMinutes = 30;
        private int maxDurationMinutes = 7 * 24 * 60;
        private int maxAdvanceBookingDays = 30;
        /** Unpaid (PENDING) reservations release their space after this many minutes. */
        private int paymentHoldMinutes = 15;
        /** How early a driver may check in before the reservation start. */
        private int earlyCheckInMinutes = 15;
        /** Confirmed reservations with no check-in this long after start become NO_SHOW. */
        private int noShowGraceMinutes = 30;
        /** Cancel at least this long before start for a full refund. */
        private int freeCancellationMinutes = 60;
        /** Drivers must verify their phone or email (one-time code) before booking. */
        private boolean requireVerifiedContact = true;
        /** Share refunded when cancelling later than that but before start. */
        private BigDecimal lateCancellationRefundRate = new BigDecimal("0.50");
    }

    @Data
    public static class Payments {
        /** "sandbox" simulates MTN MoMo / Airtel Money until real provider credentials are configured. */
        private String provider = "sandbox";
        /** Shared secret used to verify the HMAC-SHA256 signature on payment callbacks. */
        private String webhookSecret;
        /** Enables /api/payments/sandbox/** helper endpoints (never enable in production). */
        private boolean sandboxEndpointsEnabled = true;
        /**
         * Sandbox only: seconds before the simulated customer answers the prompt (0 = wait
         * for the manual /complete call). Phones ending in 000 decline, 999 never answer.
         */
        private int sandboxAutoApproveSeconds = 5;
        /** Merchant fee charged by MTN MoMo per payment (estimate; set from your contract). */
        private BigDecimal mtnMomoFeeRate = new BigDecimal("0.015");
        /** Merchant fee charged by Airtel Money per payment (estimate; set from your contract). */
        private BigDecimal airtelMoneyFeeRate = new BigDecimal("0.018");

        public BigDecimal feeRate(String method) {
            if (method == null) {
                return mtnMomoFeeRate.max(airtelMoneyFeeRate);
            }
            return switch (method) {
                case "MTN_MOMO" -> mtnMomoFeeRate;
                case "AIRTEL_MONEY" -> airtelMoneyFeeRate;
                default -> BigDecimal.ZERO; // CASH
            };
        }
    }

    private Messaging messaging = new Messaging();

    @Data
    public static class Messaging {
        /** "log" (default, prints messages) or "africastalking". */
        private String smsProvider = "log";
        /** "log" (default) or "smtp" (e.g. Brevo: spring.mail.host=smtp-relay.brevo.com). */
        private String emailProvider = "log";
        private String emailFrom = "ParkShare <no-reply@parkshare.rw>";
        /** Send messages in the background so slow providers never delay API responses. */
        private boolean async = true;
        /** Events that also go out by SMS (each SMS costs money); everything else is in-app + email. */
        private java.util.Set<String> smsEvents = new java.util.HashSet<>(java.util.Set.of(
                "RESERVATION_CONFIRMED", "RESERVATION_CANCELLED"));
        /** Send a phone-verification code right after registration. */
        private boolean verifyPhoneOnRegister = true;
        private AfricasTalking africasTalking = new AfricasTalking();
        private Otp otp = new Otp();
    }

    @Data
    public static class AfricasTalking {
        /** "sandbox" for the free test environment, or your live app username. */
        private String username = "sandbox";
        private String apiKey;
        /** Alphanumeric sender ID registered with Africa's Talking (optional). */
        private String senderId;
    }

    @Data
    public static class Otp {
        private int length = 6;
        private int ttlMinutes = 10;
        private int maxAttempts = 5;
        private int resendSeconds = 60;
        private int maxPerHour = 5;
    }

    @Data
    public static class Security {
        /** Comma-separated list of frontend origins allowed by CORS. */
        private String allowedOrigins = "http://localhost:3000,http://localhost:5173,http://127.0.0.1:5173";
        /** Lifetime of access tokens (JWT). Short, because they cannot be revoked. */
        private int accessTokenMinutes = 30;
        /** Lifetime of refresh tokens; each use replaces the token with a new one. */
        private int refreshTokenDays = 30;
        /** Optional bootstrap admin, created at startup if both are set. */
        private String adminEmail;
        private String adminPassword;
        private String adminPhone = "+250780000000";
    }
}
