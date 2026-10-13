package rw.parkshare.backend.support.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.IssueReportRequestDto;
import rw.parkshare.backend.common.dto.IssueReportResponseDto;
import rw.parkshare.backend.support.service.IssueReportService;

import java.util.List;

@RestController
@RequestMapping("/api/issue-reports")
public class IssueReportController {

    private final IssueReportService issueReportService;

    public IssueReportController(
            IssueReportService issueReportService
    ) {
        this.issueReportService = issueReportService;
    }

    @PostMapping
    public ResponseEntity<IssueReportResponseDto> createIssueReport(
            @RequestParam Long userId,
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) Long reservationId,
            @RequestBody IssueReportRequestDto request
    ) {
        IssueReportResponseDto createdIssueReport =
                issueReportService.createIssueReport(
                        userId,
                        parkingLotId,
                        reservationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdIssueReport);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueReportResponseDto> getIssueReportById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByUserId(userId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/resolved-by/{userId}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsResolvedByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsResolvedByUser(
                        userId
                )
        );
    }

    @GetMapping("/type/{issueType}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByType(
            @PathVariable String issueType
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByType(issueType)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByStatus(status)
        );
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByPriority(
            @PathVariable String priority
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByPriority(priority)
        );
    }

    @GetMapping("/status/{status}/priority/{priority}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByStatusAndPriority(
            @PathVariable String status,
            @PathVariable String priority
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByStatusAndPriority(
                        status,
                        priority
                )
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<IssueReportResponseDto>>
    getIssueReportsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                issueReportService.getIssueReportsByUserAndStatus(
                        userId,
                        status
                )
        );
    }

    @PatchMapping("/{issueReportId}/resolve")
    public ResponseEntity<IssueReportResponseDto> assignResolution(
            @PathVariable Long issueReportId,
            @RequestParam Long resolvedByUserId,
            @RequestParam String resolutionNotes
    ) {
        return ResponseEntity.ok(
                issueReportService.assignResolution(
                        issueReportId,
                        resolvedByUserId,
                        resolutionNotes
                )
        );
    }

    @PatchMapping("/{issueReportId}/status")
    public ResponseEntity<IssueReportResponseDto> updateStatus(
            @PathVariable Long issueReportId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                issueReportService.updateStatus(
                        issueReportId,
                        status
                )
        );
    }

    @PatchMapping("/{issueReportId}/priority")
    public ResponseEntity<IssueReportResponseDto> updatePriority(
            @PathVariable Long issueReportId,
            @RequestParam String priority
    ) {
        return ResponseEntity.ok(
                issueReportService.updatePriority(
                        issueReportId,
                        priority
                )
        );
    }

    @PutMapping("/{issueReportId}")
    public ResponseEntity<IssueReportResponseDto> updateIssueReport(
            @PathVariable Long issueReportId,
            @RequestBody IssueReportRequestDto request
    ) {
        return ResponseEntity.ok(
                issueReportService.updateIssueReport(
                        issueReportId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssueReport(
            @PathVariable Long id
    ) {
        issueReportService.deleteIssueReport(id);

        return ResponseEntity.noContent().build();
    }
}