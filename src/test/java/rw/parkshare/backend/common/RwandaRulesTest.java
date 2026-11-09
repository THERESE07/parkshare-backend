package rw.parkshare.backend.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rw.parkshare.backend.reservation.entity.ReservationStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static rw.parkshare.backend.reservation.entity.ReservationStatus.*;

class RwandaRulesTest {

    @ParameterizedTest
    @ValueSource(strings = {"0788123456", "788123456", "250788123456", "+250788123456", "+250 788 123 456", "078-812-3456"})
    void normalizesRwandanMobileFormats(String raw) {
        assertThat(RwandaPhone.normalize(raw)).contains("+250788123456");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0712345678", "0688123456", "+254712345678", "12345", "07881234567", "abc"})
    void rejectsNonRwandanOrMalformedNumbers(String raw) {
        assertThat(RwandaPhone.normalize(raw)).isEmpty();
    }

    @Test
    void detectsMobileNetwork() {
        assertThat(RwandaPhone.network("+250788123456")).isEqualTo(RwandaPhone.Network.MTN);
        assertThat(RwandaPhone.network("+250791234567")).isEqualTo(RwandaPhone.Network.MTN);
        assertThat(RwandaPhone.network("+250728123456")).isEqualTo(RwandaPhone.Network.AIRTEL);
        assertThat(RwandaPhone.network("+250731234567")).isEqualTo(RwandaPhone.Network.AIRTEL);
    }

    @Test
    void roundsFrancsAndExtractsInclusiveVat() {
        assertThat(Rwf.round(new BigDecimal("604"), BigDecimal.TEN)).isEqualByComparingTo("600");
        assertThat(Rwf.round(new BigDecimal("605"), BigDecimal.TEN)).isEqualByComparingTo("610");
        // 1,180 RWF including 18% VAT = 1,000 + 180
        assertThat(Rwf.vatFromInclusive(new BigDecimal("1180"), new BigDecimal("0.18"))).isEqualByComparingTo("180");
    }

    @Test
    void reservationLifecycleOnlyAllowsValidTransitions() {
        assertThat(PENDING.canTransitionTo(CONFIRMED)).isTrue();
        assertThat(PENDING.canTransitionTo(EXPIRED)).isTrue();
        assertThat(CONFIRMED.canTransitionTo(CHECKED_IN)).isTrue();
        assertThat(CHECKED_IN.canTransitionTo(COMPLETED)).isTrue();

        assertThat(PENDING.canTransitionTo(CHECKED_IN)).as("must pay before entering").isFalse();
        assertThat(CHECKED_IN.canTransitionTo(CANCELLED)).as("cannot cancel once parked").isFalse();
        assertThat(COMPLETED.canTransitionTo(PENDING)).isFalse();
        for (ReservationStatus next : ReservationStatus.values()) {
            assertThat(CANCELLED.canTransitionTo(next)).isFalse();
        }
    }
}
