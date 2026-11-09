package rw.parkshare.backend.review.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ReviewRequestDto;
import rw.parkshare.backend.common.dto.ReviewResponseDto;
import rw.parkshare.backend.review.service.ReviewService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestParam Long userId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long reservationId,
            @Valid @RequestBody ReviewRequestDto request
    ) {
        ReviewResponseDto createdReview =
                reviewService.createReview(
                        userId,
                        parkingLotId,
                        reservationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByUserId(userId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/user/{userId}/reservation/{reservationId}")
    public ResponseEntity<ReviewResponseDto> getReviewByUserAndReservation(
            @PathVariable Long userId,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewByUserAndReservation(
                        userId,
                        reservationId
                )
        );
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByRating(
            @PathVariable Integer rating
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByRating(rating)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/rating/{rating}")
    public ResponseEntity<List<ReviewResponseDto>>
    getReviewsByParkingLotAndRating(
            @PathVariable Long parkingLotId,
            @PathVariable Integer rating
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByParkingLotAndRating(
                        parkingLotId,
                        rating
                )
        );
    }

    @GetMapping("/created-between")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsCreatedBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsCreatedBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean>
    reviewExistsForUserAndReservation(
            @RequestParam Long userId,
            @RequestParam Long reservationId
    ) {
        return ResponseEntity.ok(
                reviewService.reviewExistsForUserAndReservation(
                        userId,
                        reservationId
                )
        );
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDto request
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(
                        reviewId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id
    ) {
        reviewService.deleteReview(id);

        return ResponseEntity.noContent().build();
    }
}