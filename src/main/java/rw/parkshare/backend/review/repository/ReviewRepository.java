package rw.parkshare.backend.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.review.entity.Review;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser_Id(Long userId);

    List<Review> findByParkingLot_Id(Long parkingLotId);

    List<Review> findByReservation_Id(Long reservationId);

    Optional<Review> findByUser_IdAndReservation_Id(
            Long userId,
            Long reservationId
    );

    List<Review> findByRating(Integer rating);

    List<Review> findByParkingLot_IdAndRating(
            Long parkingLotId,
            Integer rating
    );

    List<Review> findByCreatedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByUser_IdAndReservation_Id(
            Long userId,
            Long reservationId
    );
}