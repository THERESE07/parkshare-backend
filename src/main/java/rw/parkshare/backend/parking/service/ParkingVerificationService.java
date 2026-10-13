package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingVerificationRequestDto;
import rw.parkshare.backend.common.dto.ParkingVerificationResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingVerification;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingVerificationRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ParkingVerificationService {

    private final ParkingVerificationRepository verificationRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;

    public ParkingVerificationService(
            ParkingVerificationRepository verificationRepository,
            ParkingLotRepository parkingLotRepository,
            UserRepository userRepository
    ) {
        this.verificationRepository = verificationRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
    }

    public ParkingVerificationResponseDto createVerification(
            Long parkingLotId,
            ParkingVerificationRequestDto request
    ) {
        validateRequest(request);

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: "
                                        + parkingLotId
                        )
                );

        ParkingVerification verification = new ParkingVerification();

        verification.setParkingLot(parkingLot);
        verification.setVerificationStatus(
                getDefaultStatus(request.getVerificationStatus())
        );
        verification.setVerificationMethod(
                request.getVerificationMethod()
        );
        verification.setNotes(request.getNotes());

        if (verification.getCreatedAt() == null) {
            verification.setCreatedAt(OffsetDateTime.now());
        }

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    public ParkingVerificationResponseDto createVerification(
            Long parkingLotId,
            Long verifiedById,
            ParkingVerificationRequestDto request
    ) {
        validateRequest(request);

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: "
                                        + parkingLotId
                        )
                );

        User verifiedBy = userRepository.findById(verifiedById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verifier not found with ID: "
                                        + verifiedById
                        )
                );

        ParkingVerification verification = new ParkingVerification();

        verification.setParkingLot(parkingLot);
        verification.setVerifiedBy(verifiedBy);
        verification.setVerificationStatus(
                getDefaultStatus(request.getVerificationStatus())
        );
        verification.setVerificationMethod(
                request.getVerificationMethod()
        );
        verification.setNotes(request.getNotes());

        if (verification.getCreatedAt() == null) {
            verification.setCreatedAt(OffsetDateTime.now());
        }

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    @Transactional(readOnly = true)
    public ParkingVerificationResponseDto getVerificationById(Long id) {
        ParkingVerification verification =
                verificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking verification not found with ID: "
                                                + id
                                )
                        );

        return toResponseDto(verification);
    }

    @Transactional(readOnly = true)
    public List<ParkingVerificationResponseDto>
    getVerificationsByParkingLotId(Long parkingLotId) {

        return verificationRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingVerificationResponseDto>
    getVerificationsByParkingLotAndStatus(
            Long parkingLotId,
            String verificationStatus
    ) {

        return verificationRepository
                .findByParkingLot_IdAndVerificationStatus(
                        parkingLotId,
                        verificationStatus
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingVerificationResponseDto>
    getVerificationsByStatus(
            String verificationStatus
    ) {

        return verificationRepository
                .findByVerificationStatus(verificationStatus)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingVerificationResponseDto>
    getVerificationsByVerifier(Long verifiedById) {

        return verificationRepository
                .findByVerifiedBy_Id(verifiedById)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingVerificationResponseDto getLatestVerification(
            Long parkingLotId
    ) {

        ParkingVerification verification =
                verificationRepository
                        .findTopByParkingLot_IdOrderByCreatedAtDesc(
                                parkingLotId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No verification record found for parking lot with ID: "
                                                + parkingLotId
                                )
                        );

        return toResponseDto(verification);
    }

    public ParkingVerificationResponseDto approveVerification(
            Long id,
            Long verifiedById,
            String notes
    ) {

        ParkingVerification verification =
                getVerificationEntityById(id);

        User verifiedBy = userRepository.findById(verifiedById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verifier not found with ID: "
                                        + verifiedById
                        )
                );

        verification.setVerificationStatus("APPROVED");
        verification.setVerifiedBy(verifiedBy);
        verification.setVerifiedAt(OffsetDateTime.now());

        if (notes != null) {
            verification.setNotes(notes);
        }

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    public ParkingVerificationResponseDto rejectVerification(
            Long id,
            Long verifiedById,
            String notes
    ) {

        ParkingVerification verification =
                getVerificationEntityById(id);

        User verifiedBy = userRepository.findById(verifiedById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verifier not found with ID: "
                                        + verifiedById
                        )
                );

        verification.setVerificationStatus("REJECTED");
        verification.setVerifiedBy(verifiedBy);
        verification.setVerifiedAt(OffsetDateTime.now());

        if (notes != null) {
            verification.setNotes(notes);
        }

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    public ParkingVerificationResponseDto updateVerificationStatus(
            Long id,
            String verificationStatus
    ) {

        if (verificationStatus == null
                || verificationStatus.isBlank()) {

            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        ParkingVerification verification =
                getVerificationEntityById(id);

        verification.setVerificationStatus(
                verificationStatus
        );

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    public ParkingVerificationResponseDto addNotes(
            Long id,
            String notes
    ) {

        ParkingVerification verification =
                getVerificationEntityById(id);

        verification.setNotes(notes);

        ParkingVerification savedVerification =
                verificationRepository.save(verification);

        return toResponseDto(savedVerification);
    }

    public void deleteVerification(Long id) {

        ParkingVerification verification =
                getVerificationEntityById(id);

        verificationRepository.delete(verification);
    }

    private void validateRequest(
            ParkingVerificationRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking verification request is required."
            );
        }
    }

    private String getDefaultStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }

        return status;
    }

    private ParkingVerification getVerificationEntityById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking verification not found with ID: "
                                        + id
                        )
                );
    }

    private ParkingVerificationResponseDto toResponseDto(
            ParkingVerification verification
    ) {

        Long parkingLotId = null;
        Long verifiedById = null;

        if (verification.getParkingLot() != null) {
            parkingLotId =
                    verification.getParkingLot().getId();
        }

        if (verification.getVerifiedBy() != null) {
            verifiedById =
                    verification.getVerifiedBy().getId();
        }

        return new ParkingVerificationResponseDto(
                verification.getId(),
                parkingLotId,
                verifiedById,
                verification.getVerificationStatus(),
                verification.getVerificationMethod(),
                verification.getNotes(),
                verification.getVerifiedAt(),
                verification.getCreatedAt()
        );
    }
}