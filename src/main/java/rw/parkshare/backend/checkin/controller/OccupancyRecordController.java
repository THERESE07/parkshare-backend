package rw.parkshare.backend.checkin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.checkin.service.OccupancyRecordService;
import rw.parkshare.backend.common.dto.OccupancyRecordRequestDto;
import rw.parkshare.backend.common.dto.OccupancyRecordResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/occupancy-records")
public class OccupancyRecordController {

    private final OccupancyRecordService occupancyRecordService;

    public OccupancyRecordController(
            OccupancyRecordService occupancyRecordService
    ) {
        this.occupancyRecordService = occupancyRecordService;
    }

    @PostMapping
    public ResponseEntity<OccupancyRecordResponseDto> createOccupancyRecord(
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long parkingZoneId,
            @RequestBody OccupancyRecordRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                occupancyRecordService.createOccupancyRecord(
                        parkingLotId,
                        parkingZoneId,
                        request
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OccupancyRecordResponseDto> getOccupancyRecordById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<OccupancyRecordResponseDto>> getAllOccupancyRecords() {
        return ResponseEntity.ok(
                occupancyRecordService.getAllOccupancyRecords()
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-zone/{parkingZoneId}")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingZoneId(
            @PathVariable Long parkingZoneId
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingZoneId(parkingZoneId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/parking-zone/{parkingZoneId}")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingLotAndZone(
            @PathVariable Long parkingLotId,
            @PathVariable Long parkingZoneId
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingLotAndZone(
                        parkingLotId,
                        parkingZoneId
                )
        );
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsBySource(
            @PathVariable String source
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsBySource(source)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/source/{source}")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingLotAndSource(
            @PathVariable Long parkingLotId,
            @PathVariable String source
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingLotAndSource(
                        parkingLotId,
                        source
                )
        );
    }

    @GetMapping("/between")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/between")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingLotBetween(
            @PathVariable Long parkingLotId,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingLotBetween(
                        parkingLotId,
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/parking-zone/{parkingZoneId}/between")
    public ResponseEntity<List<OccupancyRecordResponseDto>> getOccupancyRecordsByParkingZoneBetween(
            @PathVariable Long parkingZoneId,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getOccupancyRecordsByParkingZoneBetween(
                        parkingZoneId,
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/latest")
    public ResponseEntity<OccupancyRecordResponseDto> getLatestOccupancyByParkingLot(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getLatestOccupancyByParkingLot(parkingLotId)
        );
    }

    @GetMapping("/parking-zone/{parkingZoneId}/latest")
    public ResponseEntity<OccupancyRecordResponseDto> getLatestOccupancyByParkingZone(
            @PathVariable Long parkingZoneId
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.getLatestOccupancyByParkingZone(parkingZoneId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<OccupancyRecordResponseDto> updateOccupancyRecord(
            @PathVariable Long id,
            @RequestBody OccupancyRecordRequestDto request
    ) {
        return ResponseEntity.ok(
                occupancyRecordService.updateOccupancyRecord(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOccupancyRecord(
            @PathVariable Long id
    ) {
        occupancyRecordService.deleteOccupancyRecord(id);
        return ResponseEntity.noContent().build();
    }
}