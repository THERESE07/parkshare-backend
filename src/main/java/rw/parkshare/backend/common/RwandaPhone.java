package rw.parkshare.backend.common;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Rwandan mobile numbers: +250 7X XXX XXXX.
 * MTN Rwanda uses 078/079, Airtel Rwanda uses 072/073.
 */
public final class RwandaPhone {

    public enum Network { MTN, AIRTEL, UNKNOWN }

    private static final Pattern NORMALIZED = Pattern.compile("^2507[2389]\\d{7}$");

    private RwandaPhone() {
    }

    /**
     * Accepts 0788123456, 788123456, 250788123456, +250 788 123 456 and
     * returns the canonical "+250788123456", or empty if invalid.
     */
    public static Optional<String> normalize(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String digits = raw.replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (!digits.chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        if (digits.startsWith("0") && digits.length() == 10) {
            digits = "250" + digits.substring(1);
        } else if (digits.length() == 9 && digits.startsWith("7")) {
            digits = "250" + digits;
        }
        if (!NORMALIZED.matcher(digits).matches()) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    public static String requireValid(String raw) {
        return normalize(raw).orElseThrow(() -> new IllegalArgumentException(
                "Phone must be a valid Rwandan mobile number, e.g. 0788123456 or +250788123456"
        ));
    }

    public static Network network(String normalized) {
        if (normalized == null || normalized.length() < 7) {
            return Network.UNKNOWN;
        }
        String prefix = normalized.substring(4, 6); // after "+250"
        return switch (prefix) {
            case "78", "79" -> Network.MTN;
            case "72", "73" -> Network.AIRTEL;
            default -> Network.UNKNOWN;
        };
    }
}
