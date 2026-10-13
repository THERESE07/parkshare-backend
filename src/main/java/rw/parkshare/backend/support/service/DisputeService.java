package rw.parkshare.backend.support.service;

import rw.parkshare.backend.common.dto.DisputeRequestDto;
import rw.parkshare.backend.common.dto.DisputeResponseDto;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.support.entity.Dispute;
import rw.parkshare.backend.support.repository.DisputeRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public DisputeService(
            DisputeRepository disputeRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository
    ) {
        this.disputeRepository = disputeRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public DisputeResponseDto createDispute(
            Long reservationId,
            Long raisedByUserId,
            DisputeRequestDto request
    ) {
        if (request.getDisputeType() == null || request.getDisputeType().isBlank()) {
            throw new IllegalArgumentException("Dispute type is required");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        if (request.getAmountDisputed() == null ||
                request.getAmountDisputed().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount disputed must be zero or greater");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        User raisedBy = userRepository.findById(raisedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Dispute dispute = new Dispute();

        dispute.setReservation(reservation);
        dispute.setRaisedBy(raisedBy);
        dispute.setDisputeType(request.getDisputeType());
        dispute.setDescription(request.getDescription());
        dispute.setAmountDisputed(request.getAmountDisputed());
        dispute.setCurrency(
                request.getCurrency() == null || request.getCurrency().isBlank()
                        ? "RWF"
                        : request.getCurrency()
        );
        dispute.setStatus(
                request.getStatus() == null || request.getStatus().isBlank()
                        ? "OPEN"
                        : request.getStatus()
        );
        dispute.setResolution(request.getResolution());

        OffsetDateTime now = OffsetDateTime.now();
        dispute.setCreatedAt(now);
        dispute.setUpdatedAt(now);

        return toResponseDto(disputeRepository.save(dispute));
    }

    public DisputeResponseDto getDisputeById(Long id) {
        return toResponseDto(
                disputeRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Dispute not found"))
        );
    }

    public List<DisputeResponseDto> getDisputesByReservationId(Long reservationId) {
        return disputeRepository.findByReservation_Id(reservationId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getDisputesRaisedByUser(Long userId) {
        return disputeRepository.findByRaisedBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getDisputesAssignedToUser(Long userId) {
        return disputeRepository.findByAssignedTo_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getDisputesByType(String disputeType) {
        return disputeRepository.findByDisputeType(disputeType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getDisputesByStatus(String status) {
        return disputeRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getDisputesByCurrency(String currency) {
        return disputeRepository.findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getAssignedDisputesByStatus(
            Long userId,
            String status
    ) {
        return disputeRepository.findByAssignedTo_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<DisputeResponseDto> getRaisedDisputesByStatus(
            Long userId,
            String status
    ) {
        return disputeRepository.findByRaisedBy_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public DisputeResponseDto assignDispute(
            Long disputeId,
            Long assignedToUserId
    ) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        User assignedTo = userRepository.findById(assignedToUserId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

        dispute.setAssignedTo(assignedTo);
        dispute.setUpdatedAt(OffsetDateTime.now());

        return toResponseDto(disputeRepository.save(dispute));
    }

    public DisputeResponseDto updateStatus(
            Long disputeId,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        dispute.setStatus(status);
        dispute.setUpdatedAt(OffsetDateTime.now());

        return toResponseDto(disputeRepository.save(dispute));
    }

    public DisputeResponseDto resolveDispute(
            Long disputeId,
            String resolution
    ) {
        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException("Resolution is required");
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        dispute.setResolution(resolution);
        dispute.setStatus("RESOLVED");
        dispute.setResolvedAt(OffsetDateTime.now());
        dispute.setUpdatedAt(OffsetDateTime.now());

        return toResponseDto(disputeRepository.save(dispute));
    }

    public DisputeResponseDto updateDispute(
            Long disputeId,
            DisputeRequestDto request
    ) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        if (request.getDisputeType() != null &&
                !request.getDisputeType().isBlank()) {
            dispute.setDisputeType(request.getDisputeType());
        }

        if (request.getDescription() != null &&
                !request.getDescription().isBlank()) {
            dispute.setDescription(request.getDescription());
        }

        if (request.getAmountDisputed() != null) {
            if (request.getAmountDisputed().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Amount disputed must be zero or greater"
                );
            }

            dispute.setAmountDisputed(request.getAmountDisputed());
        }

        if (request.getCurrency() != null &&
                !request.getCurrency().isBlank()) {
            dispute.setCurrency(request.getCurrency());
        }

        if (request.getStatus() != null &&
                !request.getStatus().isBlank()) {
            dispute.setStatus(request.getStatus());
        }

        if (request.getResolution() != null) {
            dispute.setResolution(request.getResolution());
        }

        dispute.setUpdatedAt(OffsetDateTime.now());

        return toResponseDto(disputeRepository.save(dispute));
    }

    public void deleteDispute(Long id) {
        if (!disputeRepository.existsById(id)) {
            throw new IllegalArgumentException("Dispute not found");
        }

        disputeRepository.deleteById(id);
    }

    private DisputeResponseDto toResponseDto(Dispute dispute) {
        return new DisputeResponseDto(
                dispute.getId(),
                dispute.getReservation() != null
                        ? dispute.getReservation().getId()
                        : null,
                dispute.getRaisedBy() != null
                        ? dispute.getRaisedBy().getId()
                        : null,
                dispute.getAssignedTo() != null
                        ? dispute.getAssignedTo().getId()
                        : null,
                dispute.getDisputeType(),
                dispute.getDescription(),
                dispute.getAmountDisputed(),
                dispute.getCurrency(),
                dispute.getStatus(),
                dispute.getResolution(),
                dispute.getResolvedAt(),
                dispute.getCreatedAt(),
                dispute.getUpdatedAt()
        );
    }
}