package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.SpecialRateRequestDto;
import rw.parkshare.backend.common.dto.SpecialRateResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.SpecialRate;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.SpecialRateRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class SpecialRateService {

    private final SpecialRateRepository specialRateRepository;
    private final ParkingLotRepository parkingLotRepository;

    public SpecialRateService(
            SpecialRateRepository specialRateRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        this.specialRateRepository = specialRateRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public SpecialRateResponseDto createSpecialRate(
            Long parkingLotId,
            SpecialRateRequestDto request
    ) {
        validateSpecialRate(request);

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: " + parkingLotId
                        )
                );

        SpecialRate specialRate = new SpecialRate();

        specialRate.setParkingLot(parkingLot);
        specialRate.setName(request.getName());
        specialRate.setRateType(request.getRateType());
        specialRate.setAmount(request.getAmount());
        specialRate.setCurrency(request.getCurrency());
        specialRate.setStartTime(request.getStartTime());
        specialRate.setEndTime(request.getEndTime());
        specialRate.setStartDate(request.getStartDate());
        specialRate.setEndDate(request.getEndDate());
        specialRate.setActive(request.isActive());
        specialRate.setCreatedAt(OffsetDateTime.now());
        specialRate.setUpdatedAt(OffsetDateTime.now());

        SpecialRate savedRate = specialRateRepository.save(specialRate);

        return toResponseDto(savedRate);
    }

    @Transactional(readOnly = true)
    public SpecialRateResponseDto getSpecialRateById(Long id) {
        SpecialRate specialRate = specialRateRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Special rate not found with ID: " + id
                        )
                );

        return toResponseDto(specialRate);
    }

    @Transactional(readOnly = true)
    public List<SpecialRateResponseDto> getSpecialRatesByParkingLotId(
            Long parkingLotId
    ) {
        return specialRateRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialRateResponseDto> getActiveSpecialRatesByParkingLotId(
            Long parkingLotId
    ) {
        return specialRateRepository
                .findByParkingLot_IdAndActiveTrue(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialRateResponseDto> getSpecialRatesByType(
            String rateType
    ) {
        return specialRateRepository
                .findByRateType(rateType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialRateResponseDto> getAllActiveSpecialRates() {
        return specialRateRepository
                .findByActiveTrue()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialRateResponseDto> getSpecialRatesByParkingLotAndType(
            Long parkingLotId,
            String rateType
    ) {
        return specialRateRepository
                .findByParkingLot_IdAndRateType(
                        parkingLotId,
                        rateType
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public SpecialRateResponseDto updateSpecialRate(
            Long id,
            SpecialRateRequestDto request
    ) {
        validateSpecialRate(request);

        SpecialRate existingRate = getSpecialRateEntityById(id);

        existingRate.setName(request.getName());
        existingRate.setRateType(request.getRateType());
        existingRate.setAmount(request.getAmount());
        existingRate.setCurrency(request.getCurrency());
        existingRate.setStartTime(request.getStartTime());
        existingRate.setEndTime(request.getEndTime());
        existingRate.setStartDate(request.getStartDate());
        existingRate.setEndDate(request.getEndDate());
        existingRate.setActive(request.isActive());
        existingRate.setUpdatedAt(OffsetDateTime.now());

        SpecialRate savedRate = specialRateRepository.save(existingRate);

        return toResponseDto(savedRate);
    }

    public SpecialRateResponseDto activateSpecialRate(Long id) {
        SpecialRate specialRate = getSpecialRateEntityById(id);

        specialRate.setActive(true);
        specialRate.setUpdatedAt(OffsetDateTime.now());

        SpecialRate savedRate = specialRateRepository.save(specialRate);

        return toResponseDto(savedRate);
    }

    public SpecialRateResponseDto deactivateSpecialRate(Long id) {
        SpecialRate specialRate = getSpecialRateEntityById(id);

        specialRate.setActive(false);
        specialRate.setUpdatedAt(OffsetDateTime.now());

        SpecialRate savedRate = specialRateRepository.save(specialRate);

        return toResponseDto(savedRate);
    }

    public SpecialRateResponseDto updateSpecialRateStatus(
            Long id,
            boolean active
    ) {
        SpecialRate specialRate = getSpecialRateEntityById(id);

        specialRate.setActive(active);
        specialRate.setUpdatedAt(OffsetDateTime.now());

        SpecialRate savedRate = specialRateRepository.save(specialRate);

        return toResponseDto(savedRate);
    }

    public void deleteSpecialRate(Long id) {
        SpecialRate specialRate = getSpecialRateEntityById(id);
        specialRateRepository.delete(specialRate);
    }

    private void validateSpecialRate(SpecialRateRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Special rate request is required."
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Special rate name is required."
            );
        }

        if (request.getRateType() == null || request.getRateType().isBlank()) {
            throw new IllegalArgumentException(
                    "Rate type is required."
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Special rate amount is required."
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Special rate amount cannot be negative."
            );
        }

        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required."
            );
        }

        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if ((startTime == null && endTime != null)
                || (startTime != null && endTime == null)) {
            throw new IllegalArgumentException(
                    "Both start time and end time must be provided together."
            );
        }

        if (startTime != null
                && endTime != null
                && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        OffsetDateTime startDate = request.getStartDate();
        OffsetDateTime endDate = request.getEndDate();

        if (startDate == null && endDate != null) {
            throw new IllegalArgumentException(
                    "Start date is required when end date is provided."
            );
        }

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date must be after start date."
            );
        }
    }

    private SpecialRate getSpecialRateEntityById(Long id) {
        return specialRateRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Special rate not found with ID: " + id
                        )
                );
    }

    private SpecialRateResponseDto toResponseDto(
            SpecialRate specialRate
    ) {
        Long parkingLotId = null;

        if (specialRate.getParkingLot() != null) {
            parkingLotId = specialRate.getParkingLot().getId();
        }

        return new SpecialRateResponseDto(
                specialRate.getId(),
                parkingLotId,
                specialRate.getName(),
                specialRate.getRateType(),
                specialRate.getAmount(),
                specialRate.getCurrency(),
                specialRate.getStartTime(),
                specialRate.getEndTime(),
                specialRate.getStartDate(),
                specialRate.getEndDate(),
                specialRate.isActive(),
                specialRate.getCreatedAt(),
                specialRate.getUpdatedAt()
        );
    }
}