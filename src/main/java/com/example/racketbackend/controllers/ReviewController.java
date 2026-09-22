package com.example.racketbackend.controllers;

import com.example.racketbackend.models.Review;
import com.example.racketbackend.models.User;
import com.example.racketbackend.services.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService) {

        this.reviewService = reviewService;
    }

    // Everyone can view all reviews
    @GetMapping("/reviews")
    public ResponseEntity<Object> getReviews() {
        return reviewService.getReviews();
    }

    // Everyone can view reviews for one racket
    @GetMapping("/reviews/racket/{racketId}")
    public ResponseEntity<Object> getReviewsByRacketId(
            @PathVariable Integer racketId) {

        return reviewService
                .getReviewsByRacketId(racketId);
    }

    // Purchase permission check
    @GetMapping("/reviews/can-review/{racketId}")
    public ResponseEntity<Boolean> canReview(
            @PathVariable Integer racketId,
            @AuthenticationPrincipal User loggedInUser) {

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body(false);
        }

        boolean purchased =
                reviewService.canUserReview(
                        loggedInUser.getId(),
                        racketId
                );

        return ResponseEntity.ok(purchased);
    }

    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<Object> getReviewsByUserId(
            @PathVariable Integer userId) {

        return reviewService
                .getReviewsByUserId(userId);
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<Object> getReviewById(
            @PathVariable Integer id) {

        return reviewService
                .getReviewById(id);
    }

    // Only logged-in users who purchased can submit
    @PostMapping("/reviews")
    public ResponseEntity<Object> addReview(
            @RequestBody Review review,
            @AuthenticationPrincipal User loggedInUser) {

        return reviewService.addReview(
                review,
                loggedInUser
        );
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Object> deleteReview(
            @PathVariable Integer id) {

        return reviewService.deleteReview(id);
    }

}