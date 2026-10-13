package rw.parkshare.backend.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.VehicleRequestDto;
import rw.parkshare.backend.common.dto.VehicleResponseDto;
import rw.parkshare.backend.user.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<VehicleResponseDto> createVehicle(
            @PathVariable Long userId,
            @RequestBody VehicleRequestDto request
    ) {
        VehicleResponseDto createdVehicle =
                vehicleService.createVehicle(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdVehicle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.getVehicleById(id)
        );
    }

    @GetMapping("/plate")
    public ResponseEntity<VehicleResponseDto> getVehicleByPlateNumber(
            @RequestParam String plateNumber
    ) {
        return ResponseEntity.ok(
                vehicleService.getVehicleByPlateNumber(
                        plateNumber
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                vehicleService.getVehiclesByUserId(
                        userId
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable Long id,
            @RequestBody VehicleRequestDto request
    ) {
        return ResponseEntity.ok(
                vehicleService.updateVehicle(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<VehicleResponseDto> setDefaultVehicle(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.setDefaultVehicle(id)
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<VehicleResponseDto> activateVehicle(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.activateVehicle(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<VehicleResponseDto> deactivateVehicle(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.deactivateVehicle(id)
        );
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> vehicleExists(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.vehicleExists(id)
        );
    }

    @GetMapping("/plate/exists")
    public ResponseEntity<Boolean> plateNumberExists(
            @RequestParam String plateNumber
    ) {
        return ResponseEntity.ok(
                vehicleService.plateNumberExists(
                        plateNumber
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long id
    ) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}