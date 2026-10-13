package rw.parkshare.backend.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.reservation.entity.ReservationQrCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationQrCodeRepository extends JpaRepository<ReservationQrCode, Long> {

    List<ReservationQrCode> findByReservation_Id(Long reservationId);

    Optional<ReservationQrCode> findByTokenHash(String tokenHash);

    List<ReservationQrCode> findByActiveTrue();

    List<ReservationQrCode> findByReservation_IdAndActiveTrue(Long reservationId);

    List<ReservationQrCode> findByExpiresAtBefore(OffsetDateTime dateTime);

    boolean existsByTokenHash(String tokenHash);
}
