package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingOperatingHoursRequestDto;
import rw.parkshare.backend.common.dto.ParkingOperatingHoursResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingOperatingHours;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingOperatingHoursRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class ParkingOperatingHoursService {

    private final ParkingOperatingHoursRepository operatingHoursRepository;
    private final ParkingLotRepository parkingLotRepository;

    public ParkingOperatingHoursService(
            ParkingOperatingHoursRepository operatingHoursRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        this.operatingHoursRepository = operatingHoursRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public ParkingOperatingHoursResponseDto createOperatingHours(
            Long parkingLotId,
            ParkingOperatingHoursRequestDto request
    ) {
        validateOperatingHours(request);

        if (operatingHoursRepository.existsByParkingLot_IdAndDayOfWeek(
                parkingLotId,
                request.getDayOfWeek()
        )) {
            throw new IllegalArgumentException(
                    "Operating hours already exist for this day."
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: " + parkingLotId
                        )
                );

        ParkingOperatingHours operatingHours = new ParkingOperatingHours();

        operatingHours.setParkingLot(parkingLot);
        operatingHours.setDayOfWeek(request.getDayOfWeek());
        operatingHours.setOpeningTime(request.getOpeningTime());
        operatingHours.setClosingTime(request.getClosingTime());
        operatingHours.setClosed(request.isClosed());

        ParkingOperatingHours savedHours =
                operatingHoursRepository.save(operatingHours);

        return toResponseDto(savedHours);
    }

    @Transactional(readOnly = true)
    public ParkingOperatingHoursResponseDto getOperatingHoursById(Long id) {
        ParkingOperatingHours operatingHours =
                operatingHoursRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operating hours not found with ID: " + id
                                )
                        );

        return toResponseDto(operatingHours);
    }

    @Transactional(readOnly = true)
    public List<ParkingOperatingHoursResponseDto>
    getOperatingHoursByParkingLotId(Long parkingLotId) {

        return operatingHoursRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingOperatingHoursResponseDto getOperatingHoursByDay(
            Long parkingLotId,
            String dayOfWeek
    ) {
        ParkingOperatingHours operatingHours =
                operatingHoursRepository.findByParkingLot_IdAndDayOfWeek(
                        parkingLotId,
                        dayOfWeek
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Operating hours not found for " + dayOfWeek
                        )
                );

        return toResponseDto(operatingHours);
    }

    @Transactional(readOnly = true)
    public List<ParkingOperatingHoursResponseDto>
    getOperatingHoursByClosedStatus(
            Long parkingLotId,
            boolean closed
    ) {
        return operatingHoursRepository
                .findByParkingLot_IdAndClosed(
                        parkingLotId,
                        closed
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingOperatingHoursResponseDto>
    getOperatingHoursByDay(String dayOfWeek) {

        return operatingHoursRepository
                .findByDayOfWeek(dayOfWeek)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean operatingHoursExist(
            Long parkingLotId,
            String dayOfWeek
    ) {
        return operatingHoursRepository
                .existsByParkingLot_IdAndDayOfWeek(
                        parkingLotId,
                        dayOfWeek
                );
    }

    public ParkingOperatingHoursResponseDto updateOperatingHours(
            Long id,
            ParkingOperatingHoursRequestDto request
    ) {
        validateOperatingHours(request);

        ParkingOperatingHours existingHours =
                operatingHoursRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operating hours not found with ID: " + id
                                )
                        );

        Long parkingLotId =
                existingHours.getParkingLot().getId();

        String newDayOfWeek = request.getDayOfWeek();

        if (!existingHours.getDayOfWeek().equals(newDayOfWeek)
                && operatingHoursRepository
                .existsByParkingLot_IdAndDayOfWeek(
                        parkingLotId,
                        newDayOfWeek
                )) {

            throw new IllegalArgumentException(
                    "Operating hours already exist for this day."
            );
        }

        existingHours.setDayOfWeek(newDayOfWeek);
        existingHours.setOpeningTime(request.getOpeningTime());
        existingHours.setClosingTime(request.getClosingTime());
        existingHours.setClosed(request.isClosed());

        ParkingOperatingHours savedHours =
                operatingHoursRepository.save(existingHours);

        return toResponseDto(savedHours);
    }

    public ParkingOperatingHoursResponseDto closeDay(
            Long parkingLotId,
            String dayOfWeek
    ) {
        ParkingOperatingHours hours =
                getOperatingHoursEntityByDay(
                        parkingLotId,
                        dayOfWeek
                );

        hours.setClosed(true);

        ParkingOperatingHours savedHours =
                operatingHoursRepository.save(hours);

        return toResponseDto(savedHours);
    }

    public ParkingOperatingHoursResponseDto openDay(
            Long parkingLotId,
            String dayOfWeek
    ) {
        ParkingOperatingHours hours =
                getOperatingHoursEntityByDay(
                        parkingLotId,
                        dayOfWeek
                );

        if (hours.getOpeningTime() == null
                || hours.getClosingTime() == null) {

            throw new IllegalArgumentException(
                    "Opening and closing times must be set before "
                            + "opening this day."
            );
        }

        hours.setClosed(false);

        ParkingOperatingHours savedHours =
                operatingHoursRepository.save(hours);

        return toResponseDto(savedHours);
    }

    public void deleteOperatingHours(Long id) {
        ParkingOperatingHours operatingHours =
                operatingHoursRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operating hours not found with ID: " + id
                                )
                        );

        operatingHoursRepository.delete(operatingHours);
    }

    private void validateOperatingHours(
            ParkingOperatingHoursRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Operating hours request is required."
            );
        }

        if (request.getDayOfWeek() == null
                || request.getDayOfWeek().isBlank()) {

            throw new IllegalArgumentException(
                    "Day of week is required."
            );
        }

        if (!request.isClosed()) {

            LocalTime openingTime =
                    request.getOpeningTime();

            LocalTime closingTime =
                    request.getClosingTime();

            if (openingTime == null || closingTime == null) {
                throw new IllegalArgumentException(
                        "Opening and closing times are required "
                                + "when the parking lot is open."
                );
            }

            if (!closingTime.isAfter(openingTime)) {
                throw new IllegalArgumentException(
                        "Closing time must be after opening time."
                );
            }
        }
    }

    private ParkingOperatingHours getOperatingHoursEntityByDay(
            Long parkingLotId,
            String dayOfWeek
    ) {
        return operatingHoursRepository
                .findByParkingLot_IdAndDayOfWeek(
                        parkingLotId,
                        dayOfWeek
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Operating hours not found for " + dayOfWeek
                        )
                );
    }

    private ParkingOperatingHoursResponseDto toResponseDto(
            ParkingOperatingHours operatingHours
    ) {
        Long parkingLotId = null;

        if (operatingHours.getParkingLot() != null) {
            parkingLotId =
                    operatingHours.getParkingLot().getId();
        }

        return new ParkingOperatingHoursResponseDto(
                operatingHours.getId(),
                parkingLotId,
                operatingHours.getDayOfWeek(),
                operatingHours.getOpeningTime(),
                operatingHours.getClosingTime(),
                operatingHours.isClosed()
        );
    }
}