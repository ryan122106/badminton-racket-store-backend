package com.example.racketbackend.services;

import com.example.racketbackend.dto.ReviewResponse;
import com.example.racketbackend.models.OrderStatus;
import com.example.racketbackend.models.Review;
import com.example.racketbackend.models.User;
import com.example.racketbackend.repositories.OrderItemRepository;
import com.example.racketbackend.repositories.ReviewRepository;
import com.example.racketbackend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public ResponseEntity<Object> getReviews() {
        List<Review> reviews =
                reviewRepository.findAll();

        return ResponseEntity.ok(
                convertReviews(reviews)
        );
    }

    public ResponseEntity<Object> getReviewById(
            Integer id) {

        Review review = reviewRepository
                .findById(id)
                .orElse(null);

        if (review == null) {
            return ResponseEntity.status(404)
                    .body("Review not found");
        }

        return ResponseEntity.ok(
                convertReview(review)
        );
    }

    public ResponseEntity<Object> getReviewsByRacketId(
            Integer racketId) {

        List<Review> reviews =
                reviewRepository.findByRacketId(
                        racketId
                );

        return ResponseEntity.ok(
                convertReviews(reviews)
        );
    }

    public ResponseEntity<Object> getReviewsByUserId(
            Integer userId) {

        List<Review> reviews =
                reviewRepository.findByUserId(
                        userId
                );

        return ResponseEntity.ok(
                convertReviews(reviews)
        );
    }

    /*
     * Returns true only when the user purchased this racket
     * and the order payment was successful.
     */
    public boolean canUserReview(
            Integer userId,
            Integer racketId
    ) {
        if (
                userId == null ||
                        racketId == null
        ) {
            return false;
        }

        boolean alreadyReviewed =
                reviewRepository
                        .existsByUserIdAndRacketId(
                                userId,
                                racketId
                        );

        if (alreadyReviewed) {
            return false;
        }

        List<OrderStatus> validStatuses =
                List.of(
                        OrderStatus.SUCCESSFUL,
                        OrderStatus.SHIPPED,
                        OrderStatus.COMPLETED
                );

        return orderItemRepository
                .existsByOrder_User_IdAndRacket_IdAndOrder_StatusIn(
                        userId,
                        racketId,
                        validStatuses
                );
    }

    public ResponseEntity<Object> addReview(
            Review review,
            User loggedInUser) {

        boolean alreadyReviewed =
                reviewRepository
                        .existsByUserIdAndRacketId(
                                loggedInUser.getId(),
                                review.getRacketId()
                        );

        if (alreadyReviewed) {
            return ResponseEntity
                    .status(409)
                    .body(
                            "You already reviewed this racket"
                    );
        }

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body(
                            "Please login to submit a review"
                    );
        }

        if (review.getRacketId() == null) {
            return ResponseEntity.badRequest()
                    .body(
                            "Racket ID cannot be empty"
                    );
        }

        boolean purchased =
                canUserReview(
                        loggedInUser.getId(),
                        review.getRacketId()
                );

        if (!purchased) {
            return ResponseEntity.status(403)
                    .body(
                            "You must purchase this racket " +
                                    "before writing a review"
                    );


        }

        if (review.getRating() == null ||
                review.getRating() < 1 ||
                review.getRating() > 5) {

            return ResponseEntity.badRequest()
                    .body(
                            "Rating must be between 1 and 5"
                    );
        }

        if (review.getComment() == null ||
                review.getComment().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                            "Comment is required"
                    );
        }

        /*
         * Use the user from the JWT token.
         * Do not trust userId sent from Android.
         */
        review.setId(null);
        review.setUserId(
                loggedInUser.getId()
        );

        Review savedReview =
                reviewRepository.save(review);

        return ResponseEntity.ok(
                convertReview(savedReview)
        );
    }

    public ResponseEntity<Object> deleteReview(
            Integer id) {

        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(
                            "Review not found"
                    );
        }

        reviewRepository.deleteById(id);

        return ResponseEntity.ok(
                "Review deleted successfully"
        );
    }

    private List<ReviewResponse> convertReviews(
            List<Review> reviews) {

        List<ReviewResponse> responses =
                new ArrayList<>();

        for (Review review : reviews) {
            responses.add(
                    convertReview(review)
            );
        }

        return responses;
    }

    private ReviewResponse convertReview(
            Review review) {

        User user = userRepository
                .findById(review.getUserId())
                .orElse(null);

        String userName =
                user != null
                        ? user.getName()
                        : "Unknown User";

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                userName,
                review.getRacketId(),
                review.getRating(),
                review.getComment()
        );
    }
}