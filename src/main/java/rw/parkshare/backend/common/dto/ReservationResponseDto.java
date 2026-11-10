package rw.parkshare.backend.common.dto;

import rw.parkshare.backend.reservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ReservationResponseDto {

    private Long id;
    private String reservationCode;
    private Long userId;
    private Long vehicleId;
    private Long parkingLotId;
    private Long parkingSpaceId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private ReservationStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String notes;

    public ReservationResponseDto() {
    }

    public ReservationResponseDto(
            Long id,
            String reservationCode,
            Long userId,
            Long vehicleId,
            Long parkingLotId,
            Long parkingSpaceId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            ReservationStatus status,
            BigDecimal totalAmount,
            String currency,
            String notes
    ) {
        this.id = id;
        this.reservationCode = reservationCode;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.parkingLotId = parkingLotId;
        this.parkingSpaceId = parkingSpaceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public void setParkingSpaceId(Long parkingSpaceId) {
        this.parkingSpaceId = parkingSpaceId;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** VAT (18%) included in totalAmount. */
    private BigDecimal vatAmount;

    /** Unpaid reservations are released after this time. */
    private OffsetDateTime paymentDueBy;

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public OffsetDateTime getPaymentDueBy() {
        return paymentDueBy;
    }

    public void setPaymentDueBy(OffsetDateTime paymentDueBy) {
        this.paymentDueBy = paymentDueBy;
    }
}