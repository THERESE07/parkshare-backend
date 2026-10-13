package rw.parkshare.backend.common.dto;

public class ParkingZoneResponseDto {

    private Long id;
    private Long parkingLotId;
    private String name;
    private String description;
    private Integer capacity;
    private String status;

    public ParkingZoneResponseDto() {
    }

    public ParkingZoneResponseDto(
            Long id,
            Long parkingLotId,
            String name,
            String description,
            Integer capacity,
            String status
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.status = status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
