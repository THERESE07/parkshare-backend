package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.PricingRuleRequestDto;
import rw.parkshare.backend.common.dto.PricingRuleResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.PricingRule;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.PricingRuleRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final rw.parkshare.backend.pricing.PricingService pricingService;

    public PricingRuleService(
            PricingRuleRepository pricingRuleRepository,
            ParkingLotRepository parkingLotRepository,
            rw.parkshare.backend.pricing.PricingService pricingService
    ) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.pricingService = pricingService;
    }

    public PricingRuleResponseDto createPricingRule(
            Long parkingLotId,
            PricingRuleRequestDto request
    ) {
        validatePricingRule(request);

        if (pricingRuleRepository
                .findByParkingLot_IdAndName(
                        parkingLotId,
                        request.getName()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A pricing rule with this name already exists for the parking lot."
            );
        }

        ParkingLot parkingLot =
                parkingLotRepository.findById(parkingLotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking lot not found with ID: "
                                                + parkingLotId
                                )
                        );

        PricingRule pricingRule = new PricingRule();

        pricingRule.setParkingLot(parkingLot);
        pricingRule.setName(request.getName());
        pricingRule.setPricingType(request.getPricingType());
        pricingRule.setAmount(request.getAmount());
        pricingRule.setCurrency(request.getCurrency());
        pricingRule.setMinimumDurationMinutes(
                request.getMinimumDurationMinutes()
        );
        pricingRule.setMaximumDurationMinutes(
                request.getMaximumDurationMinutes()
        );
        pricingRule.setActive(request.isActive());

        if (request.getEffectiveFrom() == null) {
            pricingRule.setEffectiveFrom(OffsetDateTime.now());
        } else {
            pricingRule.setEffectiveFrom(
                    request.getEffectiveFrom()
            );
        }

        pricingRule.setEffectiveUntil(
                request.getEffectiveUntil()
        );

        pricingRule.setCreatedAt(OffsetDateTime.now());
        pricingRule.setUpdatedAt(OffsetDateTime.now());

        PricingRule savedRule =
                pricingRuleRepository.save(pricingRule);

        return toResponseDto(savedRule);
    }

    @Transactional(readOnly = true)
    public PricingRuleResponseDto getPricingRuleById(Long id) {

        PricingRule pricingRule =
                pricingRuleRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pricing rule not found with ID: "
                                                + id
                                )
                        );

        return toResponseDto(pricingRule);
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getPricingRulesByParkingLotId(Long parkingLotId) {

        return pricingRuleRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getActivePricingRulesByParkingLotId(
            Long parkingLotId
    ) {

        return pricingRuleRepository
                .findByParkingLot_IdAndActiveTrue(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getPricingRulesByType(
            Long parkingLotId,
            String pricingType
    ) {

        return pricingRuleRepository
                .findByParkingLot_IdAndPricingType(
                        parkingLotId,
                        pricingType
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getAllActivePricingRules() {

        return pricingRuleRepository
                .findByActiveTrue()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PricingRuleResponseDto getPricingRuleByName(
            Long parkingLotId,
            String name
    ) {

        PricingRule pricingRule =
                pricingRuleRepository
                        .findByParkingLot_IdAndName(
                                parkingLotId,
                                name
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pricing rule not found with name: "
                                                + name
                                )
                        );

        return toResponseDto(pricingRule);
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getPricingRulesByCurrency(String currency) {

        return pricingRuleRepository
                .findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto>
    getPricingRulesByParkingLotAndCurrency(
            Long parkingLotId,
            String currency
    ) {

        return pricingRuleRepository
                .findByParkingLot_IdAndCurrency(
                        parkingLotId,
                        currency
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public PricingRuleResponseDto updatePricingRule(
            Long id,
            PricingRuleRequestDto request
    ) {
        validatePricingRule(request);

        PricingRule existingRule =
                getPricingRuleEntityById(id);

        Long parkingLotId =
                existingRule.getParkingLot().getId();

        if (!existingRule.getName().equals(request.getName())
                && pricingRuleRepository
                .findByParkingLot_IdAndName(
                        parkingLotId,
                        request.getName()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A pricing rule with this name already exists for the parking lot."
            );
        }

        existingRule.setName(request.getName());
        existingRule.setPricingType(
                request.getPricingType()
        );
        existingRule.setAmount(
                request.getAmount()
        );
        existingRule.setCurrency(
                request.getCurrency()
        );
        existingRule.setMinimumDurationMinutes(
                request.getMinimumDurationMinutes()
        );
        existingRule.setMaximumDurationMinutes(
                request.getMaximumDurationMinutes()
        );
        existingRule.setEffectiveFrom(
                request.getEffectiveFrom()
        );
        existingRule.setEffectiveUntil(
                request.getEffectiveUntil()
        );
        existingRule.setActive(
                request.isActive()
        );
        existingRule.setUpdatedAt(
                OffsetDateTime.now()
        );

        PricingRule savedRule =
                pricingRuleRepository.save(existingRule);

        return toResponseDto(savedRule);
    }

    public PricingRuleResponseDto activatePricingRule(Long id) {

        PricingRule pricingRule =
                getPricingRuleEntityById(id);

        pricingRule.setActive(true);
        pricingRule.setUpdatedAt(OffsetDateTime.now());

        PricingRule savedRule =
                pricingRuleRepository.save(pricingRule);

        return toResponseDto(savedRule);
    }

    public PricingRuleResponseDto deactivatePricingRule(Long id) {

        PricingRule pricingRule =
                getPricingRuleEntityById(id);

        pricingRule.setActive(false);
        pricingRule.setUpdatedAt(OffsetDateTime.now());

        PricingRule savedRule =
                pricingRuleRepository.save(pricingRule);

        return toResponseDto(savedRule);
    }

    public PricingRuleResponseDto updatePricingRuleStatus(
            Long id,
            boolean active
    ) {

        PricingRule pricingRule =
                getPricingRuleEntityById(id);

        pricingRule.setActive(active);
        pricingRule.setUpdatedAt(OffsetDateTime.now());

        PricingRule savedRule =
                pricingRuleRepository.save(pricingRule);

        return toResponseDto(savedRule);
    }

    public void deletePricingRule(Long id) {

        PricingRule pricingRule =
                getPricingRuleEntityById(id);

        pricingRuleRepository.delete(pricingRule);
    }

    private void validatePricingRule(
            PricingRuleRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Pricing rule request is required."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Pricing rule name is required."
            );
        }

        if (request.getPricingType() == null
                || request.getPricingType().isBlank()) {

            throw new IllegalArgumentException(
                    "Pricing type is required."
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Pricing amount is required."
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Pricing amount cannot be negative."
            );
        }

        pricingService.validateOperatorRate(request.getPricingType(), amount);

        if (request.getCurrency() == null
                || request.getCurrency().isBlank()) {

            throw new IllegalArgumentException(
                    "Currency is required."
            );
        }

        Integer minimumDuration =
                request.getMinimumDurationMinutes();

        Integer maximumDuration =
                request.getMaximumDurationMinutes();

        if (minimumDuration != null
                && minimumDuration < 0) {

            throw new IllegalArgumentException(
                    "Minimum duration cannot be negative."
            );
        }

        if (maximumDuration != null
                && maximumDuration < 0) {

            throw new IllegalArgumentException(
                    "Maximum duration cannot be negative."
            );
        }

        if (minimumDuration != null
                && maximumDuration != null
                && maximumDuration < minimumDuration) {

            throw new IllegalArgumentException(
                    "Maximum duration must be greater than or equal to minimum duration."
            );
        }

        if (request.getEffectiveFrom() != null
                && request.getEffectiveUntil() != null
                && request.getEffectiveUntil()
                .isBefore(request.getEffectiveFrom())) {

            throw new IllegalArgumentException(
                    "Effective until must be after effective from."
            );
        }
    }

    private PricingRule getPricingRuleEntityById(Long id) {

        return pricingRuleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pricing rule not found with ID: "
                                        + id
                        )
                );
    }

    private PricingRuleResponseDto toResponseDto(
            PricingRule pricingRule
    ) {

        Long parkingLotId = null;

        if (pricingRule.getParkingLot() != null) {
            parkingLotId =
                    pricingRule.getParkingLot().getId();
        }

        return new PricingRuleResponseDto(
                pricingRule.getId(),
                parkingLotId,
                pricingRule.getName(),
                pricingRule.getPricingType(),
                pricingRule.getAmount(),
                pricingRule.getCurrency(),
                pricingRule.getMinimumDurationMinutes(),
                pricingRule.getMaximumDurationMinutes(),
                pricingRule.isActive(),
                pricingRule.getEffectiveFrom(),
                pricingRule.getEffectiveUntil(),
                pricingRule.getCreatedAt(),
                pricingRule.getUpdatedAt()
        );
    }
}