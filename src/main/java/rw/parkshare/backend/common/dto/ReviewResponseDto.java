package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ReviewResponseDto {

    private Long id;
    private Long userId;
    private Long parkingLotId;
    private Long reservationId;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ReviewResponseDto() {
    }

    public ReviewResponseDto(
            Long id,
            Long userId,
            Long parkingLotId,
            Long reservationId,
            Integer rating,
            String comment,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.parkingLotId = parkingLotId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}