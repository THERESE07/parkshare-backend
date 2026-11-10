package rw.parkshare.backend.reservation.entity;

import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {

    PENDING,
    CONFIRMED,
    CANCELLED,
    CHECKED_IN,
    COMPLETED,
    EXPIRED,
    NO_SHOW;

    /** Statuses that hold a parking space for their time window. */
    public static final Set<ReservationStatus> ACTIVE = EnumSet.of(PENDING, CONFIRMED, CHECKED_IN);

    /**
     * Allowed lifecycle:
     * PENDING (awaiting payment) -> CONFIRMED (paid) -> CHECKED_IN -> COMPLETED.
     * PENDING may expire or be cancelled; CONFIRMED may be cancelled or become NO_SHOW.
     */
    public boolean canTransitionTo(ReservationStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED || next == EXPIRED;
            case CONFIRMED -> next == CHECKED_IN || next == CANCELLED || next == NO_SHOW;
            case CHECKED_IN -> next == COMPLETED;
            case CANCELLED, COMPLETED, EXPIRED, NO_SHOW -> false;
        };
    }

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
