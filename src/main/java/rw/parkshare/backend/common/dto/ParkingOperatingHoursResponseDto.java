package rw.parkshare.backend.common.dto;

import java.time.LocalTime;

public class ParkingOperatingHoursResponseDto {

    private Long id;
    private Long parkingLotId;
    private String dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean closed;

    public ParkingOperatingHoursResponseDto() {
    }

    public ParkingOperatingHoursResponseDto(
            Long id,
            Long parkingLotId,
            String dayOfWeek,
            LocalTime openingTime,
            LocalTime closingTime,
            boolean closed
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.dayOfWeek = dayOfWeek;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.closed = closed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}