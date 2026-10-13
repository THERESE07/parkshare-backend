package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ReservationQrCodeRequestDto {

    private String tokenHash;
    private OffsetDateTime expiresAt;

    public ReservationQrCodeRequestDto() {
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
}