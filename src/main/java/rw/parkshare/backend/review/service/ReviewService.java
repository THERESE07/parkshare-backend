package rw.parkshare.backend.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ReviewRequestDto;
import rw.parkshare.backend.common.dto.ReviewResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.review.entity.Review;
import rw.parkshare.backend.review.repository.ReviewRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ReservationRepository reservationRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ParkingLotRepository parkingLotRepository,
            ReservationRepository reservationRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReviewResponseDto createReview(
            Long userId,
            Long parkingLotId,
            Long reservationId,
            ReviewRequestDto request
    ) {
        if (request.getRating() == null) {
            throw new IllegalArgumentException(
                    "Review rating is required."
            );
        }

        if (request.getRating() < 1
                || request.getRating() > 5) {
            throw new IllegalArgumentException(
                    "Review rating must be between 1 and 5."
            );
        }

        if (reservationId != null
                && reviewRepository.existsByUser_IdAndReservation_Id(
                userId,
                reservationId
        )) {
            throw new IllegalArgumentException(
                    "This user has already reviewed this reservation."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        ParkingLot parkingLot =
                parkingLotRepository.findById(parkingLotId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking lot not found with ID: "
                                                + parkingLotId
                                )
                        );

        Review review = new Review();

        review.setUser(user);
        review.setParkingLot(parkingLot);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (reservationId != null) {
            Reservation reservation =
                    reservationRepository.findById(reservationId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Reservation not found with ID: "
                                                    + reservationId
                                    )
                            );

            review.setReservation(reservation);
        }

        OffsetDateTime now = OffsetDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        return toResponseDto(
                reviewRepository.save(review)
        );
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long id) {
        return toResponseDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByUserId(
            Long userId
    ) {
        return reviewRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByParkingLotId(
            Long parkingLotId
    ) {
        return reviewRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByReservationId(
            Long reservationId
    ) {
        return reviewRepository.findByReservation_Id(reservationId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewByUserAndReservation(
            Long userId,
            Long reservationId
    ) {
        Review review =
                reviewRepository
                        .findByUser_IdAndReservation_Id(
                                userId,
                                reservationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Review not found for this user and reservation."
                                )
                        );

        return toResponseDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByRating(
            Integer rating
    ) {
        return reviewRepository.findByRating(rating)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByParkingLotAndRating(
            Long parkingLotId,
            Integer rating
    ) {
        return reviewRepository
                .findByParkingLot_IdAndRating(
                        parkingLotId,
                        rating
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsCreatedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return reviewRepository.findByCreatedAtBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean reviewExistsForUserAndReservation(
            Long userId,
            Long reservationId
    ) {
        return reviewRepository
                .existsByUser_IdAndReservation_Id(
                        userId,
                        reservationId
                );
    }

    public ReviewResponseDto updateReview(
            Long reviewId,
            ReviewRequestDto request
    ) {
        if (request.getRating() != null
                && (request.getRating() < 1
                || request.getRating() > 5)) {
            throw new IllegalArgumentException(
                    "Review rating must be between 1 and 5."
            );
        }

        Review review = getEntityById(reviewId);

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        return toResponseDto(
                reviewRepository.save(review)
        );
    }

    public void deleteReview(Long id) {
        Review review = getEntityById(id);

        reviewRepository.delete(review);
    }

    private Review getEntityById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Review not found with ID: " + id
                        )
                );
    }

    private ReviewResponseDto toResponseDto(
            Review review
    ) {
        return new ReviewResponseDto(
                review.getId(),
                review.getUser() != null
                        ? review.getUser().getId()
                        : null,
                review.getParkingLot() != null
                        ? review.getParkingLot().getId()
                        : null,
                review.getReservation() != null
                        ? review.getReservation().getId()
                        : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}