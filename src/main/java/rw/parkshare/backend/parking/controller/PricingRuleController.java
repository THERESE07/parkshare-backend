package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.PricingRuleRequestDto;
import rw.parkshare.backend.common.dto.PricingRuleResponseDto;
import rw.parkshare.backend.parking.service.PricingRuleService;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-rules")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    public PricingRuleController(
            PricingRuleService pricingRuleService
    ) {
        this.pricingRuleService = pricingRuleService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<PricingRuleResponseDto> createPricingRule(
            @PathVariable Long parkingLotId,
            @RequestBody PricingRuleRequestDto request
    ) {
        PricingRuleResponseDto createdRule =
                pricingRuleService.createPricingRule(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingRuleResponseDto> getPricingRuleById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pricingRuleService.getPricingRuleById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getPricingRulesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                pricingRuleService
                        .getPricingRulesByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/active")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getActivePricingRulesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                pricingRuleService
                        .getActivePricingRulesByParkingLotId(
                                parkingLotId
                        )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/type")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getPricingRulesByType(
            @PathVariable Long parkingLotId,
            @RequestParam String pricingType
    ) {
        return ResponseEntity.ok(
                pricingRuleService.getPricingRulesByType(
                        parkingLotId,
                        pricingType
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getAllActivePricingRules() {
        return ResponseEntity.ok(
                pricingRuleService.getAllActivePricingRules()
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/name")
    public ResponseEntity<PricingRuleResponseDto> getPricingRuleByName(
            @PathVariable Long parkingLotId,
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                pricingRuleService.getPricingRuleByName(
                        parkingLotId,
                        name
                )
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getPricingRulesByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                pricingRuleService.getPricingRulesByCurrency(
                        currency
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/currency/{currency}")
    public ResponseEntity<List<PricingRuleResponseDto>>
    getPricingRulesByParkingLotAndCurrency(
            @PathVariable Long parkingLotId,
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                pricingRuleService
                        .getPricingRulesByParkingLotAndCurrency(
                                parkingLotId,
                                currency
                        )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingRuleResponseDto> updatePricingRule(
            @PathVariable Long id,
            @RequestBody PricingRuleRequestDto request
    ) {
        return ResponseEntity.ok(
                pricingRuleService.updatePricingRule(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PricingRuleResponseDto> activatePricingRule(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pricingRuleService.activatePricingRule(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PricingRuleResponseDto> deactivatePricingRule(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pricingRuleService.deactivatePricingRule(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PricingRuleResponseDto>
    updatePricingRuleStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(
                pricingRuleService.updatePricingRuleStatus(
                        id,
                        active
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricingRule(
            @PathVariable Long id
    ) {
        pricingRuleService.deletePricingRule(id);

        return ResponseEntity.noContent().build();
    }
}