package rw.parkshare.backend.support.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.IssueReportRequestDto;
import rw.parkshare.backend.common.dto.IssueReportResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.support.entity.IssueReport;
import rw.parkshare.backend.support.repository.IssueReportRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class IssueReportService {

    private final IssueReportRepository issueReportRepository;
    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ReservationRepository reservationRepository;

    public IssueReportService(
            IssueReportRepository issueReportRepository,
            UserRepository userRepository,
            ParkingLotRepository parkingLotRepository,
            ReservationRepository reservationRepository
    ) {
        this.issueReportRepository = issueReportRepository;
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.reservationRepository = reservationRepository;
    }

    public IssueReportResponseDto createIssueReport(
            Long userId,
            Long parkingLotId,
            Long reservationId,
            IssueReportRequestDto request
    ) {
        if (request.getIssueType() == null
                || request.getIssueType().isBlank()) {
            throw new IllegalArgumentException("Issue type is required.");
        }

        if (request.getTitle() == null
                || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Issue title is required.");
        }

        if (request.getDescription() == null
                || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Issue description is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        IssueReport issueReport = new IssueReport();

        issueReport.setUser(user);
        issueReport.setIssueType(request.getIssueType());
        issueReport.setTitle(request.getTitle());
        issueReport.setDescription(request.getDescription());

        issueReport.setStatus(
                request.getStatus() == null
                        || request.getStatus().isBlank()
                        ? "OPEN"
                        : request.getStatus()
        );

        issueReport.setPriority(
                request.getPriority() == null
                        || request.getPriority().isBlank()
                        ? "MEDIUM"
                        : request.getPriority()
        );

        issueReport.setResolutionNotes(request.getResolutionNotes());

        if (parkingLotId != null) {
            ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parking lot not found with ID: "
                                            + parkingLotId
                            )
                    );

            issueReport.setParkingLot(parkingLot);
        }

        if (reservationId != null) {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Reservation not found with ID: "
                                            + reservationId
                            )
                    );

            issueReport.setReservation(reservation);
        }

        OffsetDateTime now = OffsetDateTime.now();
        issueReport.setCreatedAt(now);
        issueReport.setUpdatedAt(now);

        return toResponseDto(
                issueReportRepository.save(issueReport)
        );
    }

    @Transactional(readOnly = true)
    public IssueReportResponseDto getIssueReportById(Long id) {
        return toResponseDto(
                issueReportRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Issue report not found with ID: " + id
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByUserId(
            Long userId
    ) {
        return issueReportRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByParkingLotId(
            Long parkingLotId
    ) {
        return issueReportRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByReservationId(
            Long reservationId
    ) {
        return issueReportRepository.findByReservation_Id(reservationId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsResolvedByUser(
            Long userId
    ) {
        return issueReportRepository.findByResolvedBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByType(
            String issueType
    ) {
        return issueReportRepository.findByIssueType(issueType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByStatus(
            String status
    ) {
        return issueReportRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByPriority(
            String priority
    ) {
        return issueReportRepository.findByPriority(priority)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByStatusAndPriority(
            String status,
            String priority
    ) {
        return issueReportRepository
                .findByStatusAndPriority(status, priority)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponseDto> getIssueReportsByUserAndStatus(
            Long userId,
            String status
    ) {
        return issueReportRepository
                .findByUser_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public IssueReportResponseDto assignResolution(
            Long issueReportId,
            Long resolvedByUserId,
            String resolutionNotes
    ) {
        if (resolutionNotes == null
                || resolutionNotes.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolution notes are required."
            );
        }

        IssueReport issueReport = getEntityById(issueReportId);

        User resolvedBy = userRepository.findById(resolvedByUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: "
                                        + resolvedByUserId
                        )
                );

        issueReport.setResolvedBy(resolvedBy);
        issueReport.setResolutionNotes(resolutionNotes);
        issueReport.setResolvedAt(OffsetDateTime.now());
        issueReport.setStatus("RESOLVED");

        return toResponseDto(
                issueReportRepository.save(issueReport)
        );
    }

    public IssueReportResponseDto updateStatus(
            Long issueReportId,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Issue report status is required."
            );
        }

        IssueReport issueReport = getEntityById(issueReportId);
        issueReport.setStatus(status);

        return toResponseDto(
                issueReportRepository.save(issueReport)
        );
    }

    public IssueReportResponseDto updatePriority(
            Long issueReportId,
            String priority
    ) {
        if (priority == null || priority.isBlank()) {
            throw new IllegalArgumentException(
                    "Issue report priority is required."
            );
        }

        IssueReport issueReport = getEntityById(issueReportId);
        issueReport.setPriority(priority);

        return toResponseDto(
                issueReportRepository.save(issueReport)
        );
    }

    public IssueReportResponseDto updateIssueReport(
            Long issueReportId,
            IssueReportRequestDto request
    ) {
        IssueReport issueReport = getEntityById(issueReportId);

        if (request.getIssueType() != null
                && !request.getIssueType().isBlank()) {
            issueReport.setIssueType(request.getIssueType());
        }

        if (request.getTitle() != null
                && !request.getTitle().isBlank()) {
            issueReport.setTitle(request.getTitle());
        }

        if (request.getDescription() != null
                && !request.getDescription().isBlank()) {
            issueReport.setDescription(request.getDescription());
        }

        if (request.getPriority() != null
                && !request.getPriority().isBlank()) {
            issueReport.setPriority(request.getPriority());
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            issueReport.setStatus(request.getStatus());
        }

        if (request.getResolutionNotes() != null) {
            issueReport.setResolutionNotes(
                    request.getResolutionNotes()
            );
        }

        return toResponseDto(
                issueReportRepository.save(issueReport)
        );
    }

    public void deleteIssueReport(Long id) {
        IssueReport issueReport = getEntityById(id);
        issueReportRepository.delete(issueReport);
    }

    private IssueReport getEntityById(Long id) {
        return issueReportRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Issue report not found with ID: " + id
                        )
                );
    }

    private IssueReportResponseDto toResponseDto(
            IssueReport issueReport
    ) {
        return new IssueReportResponseDto(
                issueReport.getId(),

                issueReport.getUser() != null
                        ? issueReport.getUser().getId()
                        : null,

                issueReport.getParkingLot() != null
                        ? issueReport.getParkingLot().getId()
                        : null,

                issueReport.getReservation() != null
                        ? issueReport.getReservation().getId()
                        : null,

                issueReport.getIssueType(),
                issueReport.getTitle(),
                issueReport.getDescription(),
                issueReport.getStatus(),
                issueReport.getPriority(),

                issueReport.getResolvedBy() != null
                        ? issueReport.getResolvedBy().getId()
                        : null,

                issueReport.getResolvedAt(),
                issueReport.getResolutionNotes(),
                issueReport.getCreatedAt(),
                issueReport.getUpdatedAt()
        );
    }
}