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
    }

    @Data
    public static class Security {
        /** Comma-separated list of frontend origins allowed by CORS. */
        private String allowedOrigins = "http://localhost:3000,http://localhost:5173";
        /** Optional bootstrap admin, created at startup if both are set. */
        private String adminEmail;
        private String adminPassword;
        private String adminPhone = "+250780000000";
    }
}
