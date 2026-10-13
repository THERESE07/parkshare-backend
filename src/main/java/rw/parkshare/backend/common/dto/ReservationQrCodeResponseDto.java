package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ReservationQrCodeResponseDto {

    private Long id;
    private Long reservationId;
    private String tokenHash;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
    private boolean active;
    private OffsetDateTime createdAt;

    public ReservationQrCodeResponseDto() {
    }

    public ReservationQrCodeResponseDto(
            Long id,
            Long reservationId,
            String tokenHash,
            OffsetDateTime expiresAt,
            OffsetDateTime usedAt,
            boolean active,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}