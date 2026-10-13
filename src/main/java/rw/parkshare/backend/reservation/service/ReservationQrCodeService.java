package rw.parkshare.backend.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ReservationQrCodeRequestDto;
import rw.parkshare.backend.common.dto.ReservationQrCodeResponseDto;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationQrCode;
import rw.parkshare.backend.reservation.repository.ReservationQrCodeRepository;
import rw.parkshare.backend.reservation.repository.ReservationRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationQrCodeService {

    private final ReservationQrCodeRepository qrCodeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationQrCodeService(
            ReservationQrCodeRepository qrCodeRepository,
            ReservationRepository reservationRepository
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationQrCodeResponseDto createQrCode(
            Long reservationId,
            ReservationQrCodeRequestDto request
    ) {
        if (request.getTokenHash() == null
                || request.getTokenHash().isBlank()) {
            throw new IllegalArgumentException(
                    "QR token hash is required."
            );
        }

        if (qrCodeRepository.existsByTokenHash(
                request.getTokenHash()
        )) {
            throw new IllegalArgumentException(
                    "A QR code with this token hash already exists."
            );
        }

        if (request.getExpiresAt() == null) {
            throw new IllegalArgumentException(
                    "QR code expiration time is required."
            );
        }

        if (!request.getExpiresAt().isAfter(
                OffsetDateTime.now()
        )) {
            throw new IllegalArgumentException(
                    "QR code expiration time must be in the future."
            );
        }

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found with ID: "
                                                + reservationId
                                )
                        );

        ReservationQrCode qrCode = new ReservationQrCode();

        qrCode.setReservation(reservation);
        qrCode.setTokenHash(request.getTokenHash());
        qrCode.setExpiresAt(request.getExpiresAt());
        qrCode.setActive(true);
        qrCode.setCreatedAt(OffsetDateTime.now());

        ReservationQrCode savedQrCode =
                qrCodeRepository.save(qrCode);

        return toResponseDto(savedQrCode);
    }

    @Transactional(readOnly = true)
    public ReservationQrCodeResponseDto getQrCodeById(Long id) {
        return toResponseDto(getQrCodeEntityById(id));
    }

    @Transactional(readOnly = true)
    public ReservationQrCodeResponseDto getQrCodeByTokenHash(
            String tokenHash
    ) {
        ReservationQrCode qrCode =
                qrCodeRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation QR code not found with the provided token."
                                )
                        );

        return toResponseDto(qrCode);
    }

    @Transactional(readOnly = true)
    public List<ReservationQrCodeResponseDto>
    getQrCodesByReservationId(Long reservationId) {

        return qrCodeRepository.findByReservation_Id(
                        reservationId
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationQrCodeResponseDto> getActiveQrCodes() {

        return qrCodeRepository.findByActiveTrue()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationQrCodeResponseDto>
    getActiveQrCodesByReservationId(Long reservationId) {

        return qrCodeRepository
                .findByReservation_IdAndActiveTrue(
                        reservationId
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationQrCodeResponseDto>
    getExpiredQrCodes() {

        return qrCodeRepository.findByExpiresAtBefore(
                        OffsetDateTime.now()
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean tokenHashExists(String tokenHash) {
        return qrCodeRepository.existsByTokenHash(tokenHash);
    }

    public ReservationQrCodeResponseDto deactivateQrCode(Long id) {

        ReservationQrCode qrCode =
                getQrCodeEntityById(id);

        qrCode.setActive(false);

        return toResponseDto(
                qrCodeRepository.save(qrCode)
        );
    }

    public ReservationQrCodeResponseDto activateQrCode(Long id) {

        ReservationQrCode qrCode =
                getQrCodeEntityById(id);

        if (qrCode.getExpiresAt().isBefore(
                OffsetDateTime.now()
        )) {
            throw new IllegalArgumentException(
                    "An expired QR code cannot be activated."
            );
        }

        if (qrCode.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "A used QR code cannot be activated."
            );
        }

        qrCode.setActive(true);

        return toResponseDto(
                qrCodeRepository.save(qrCode)
        );
    }

    public ReservationQrCodeResponseDto markAsUsed(Long id) {

        ReservationQrCode qrCode =
                getQrCodeEntityById(id);

        if (!qrCode.isActive()) {
            throw new IllegalArgumentException(
                    "This QR code is not active."
            );
        }

        if (qrCode.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "This QR code has already been used."
            );
        }

        if (!qrCode.getExpiresAt().isAfter(
                OffsetDateTime.now()
        )) {
            throw new IllegalArgumentException(
                    "This QR code has expired."
            );
        }

        qrCode.setUsedAt(OffsetDateTime.now());
        qrCode.setActive(false);

        return toResponseDto(
                qrCodeRepository.save(qrCode)
        );
    }

    public int deactivateExpiredQrCodes() {

        List<ReservationQrCode> expiredQrCodes =
                qrCodeRepository.findByExpiresAtBefore(
                        OffsetDateTime.now()
                );

        int deactivatedCount = 0;

        for (ReservationQrCode qrCode : expiredQrCodes) {
            if (qrCode.isActive()) {
                qrCode.setActive(false);
                deactivatedCount++;
            }
        }

        if (deactivatedCount > 0) {
            qrCodeRepository.saveAll(expiredQrCodes);
        }

        return deactivatedCount;
    }

    public ReservationQrCodeResponseDto updateExpiration(
            Long id,
            OffsetDateTime expiresAt
    ) {
        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "QR code expiration time is required."
            );
        }

        if (!expiresAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException(
                    "QR code expiration time must be in the future."
            );
        }

        ReservationQrCode qrCode =
                getQrCodeEntityById(id);

        if (qrCode.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "A used QR code expiration cannot be changed."
            );
        }

        qrCode.setExpiresAt(expiresAt);

        return toResponseDto(
                qrCodeRepository.save(qrCode)
        );
    }

    public void deleteQrCode(Long id) {

        ReservationQrCode qrCode =
                getQrCodeEntityById(id);

        qrCodeRepository.delete(qrCode);
    }

    private ReservationQrCode getQrCodeEntityById(Long id) {

        return qrCodeRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation QR code not found with ID: "
                                        + id
                        )
                );
    }

    private ReservationQrCodeResponseDto toResponseDto(
            ReservationQrCode qrCode
    ) {
        return new ReservationQrCodeResponseDto(
                qrCode.getId(),
                qrCode.getReservation().getId(),
                qrCode.getTokenHash(),
                qrCode.getExpiresAt(),
                qrCode.getUsedAt(),
                qrCode.isActive(),
                qrCode.getCreatedAt()
        );
    }
}