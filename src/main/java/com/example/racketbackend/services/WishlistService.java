package com.example.racketbackend.services;

import com.example.racketbackend.dto.WishlistResponse;
import com.example.racketbackend.models.Racket;
import com.example.racketbackend.models.Wishlist;
import com.example.racketbackend.repositories.RacketRepository;
import com.example.racketbackend.repositories.WishlistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final RacketRepository racketRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            RacketRepository racketRepository
    ) {
        this.wishlistRepository =
                wishlistRepository;

        this.racketRepository =
                racketRepository;
    }

    public ResponseEntity<Object> getMyWishlist(
            Integer userId
    ) {

        List<Wishlist> wishlists =
                wishlistRepository.findByUserId(
                        userId
                );

        List<WishlistResponse> responses =
                new ArrayList<>();

        for (Wishlist wishlist : wishlists) {

            Racket racket =
                    racketRepository
                            .findById(
                                    wishlist.getRacketId()
                            )
                            .orElse(null);

            if (racket != null) {

                responses.add(
                        new WishlistResponse(
                                wishlist.getId(),
                                racket
                        )
                );
            }
        }

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<Object> addWishlist(
            Integer userId,
            Integer racketId
    ) {

        Racket racket =
                racketRepository
                        .findById(racketId)
                        .orElse(null);

        if (racket == null) {

            return ResponseEntity.status(404)
                    .body("Racket not found");
        }

        boolean alreadyExists =
                wishlistRepository
                        .existsByUserIdAndRacketId(
                                userId,
                                racketId
                        );

        if (alreadyExists) {

            return ResponseEntity.status(409)
                    .body(
                            "Racket is already in your wishlist"
                    );
        }

        Wishlist wishlist =
                new Wishlist();

        wishlist.setUserId(userId);
        wishlist.setRacketId(racketId);

        Wishlist savedWishlist =
                wishlistRepository.save(
                        wishlist
                );

        WishlistResponse response =
                new WishlistResponse(
                        savedWishlist.getId(),
                        racket
                );

        return ResponseEntity.status(201)
                .body(response);
    }

    public ResponseEntity<Object> removeWishlist(
            Integer userId,
            Integer racketId
    ) {

        Wishlist wishlist =
                wishlistRepository
                        .findByUserIdAndRacketId(
                                userId,
                                racketId
                        )
                        .orElse(null);

        if (wishlist == null) {

            return ResponseEntity.status(404)
                    .body(
                            "Racket is not in your wishlist"
                    );
        }

        wishlistRepository.delete(wishlist);

        return ResponseEntity.ok(
                "Racket removed from wishlist"
        );
    }

    public ResponseEntity<Object> checkWishlist(
            Integer userId,
            Integer racketId
    ) {

        boolean isWishlisted =
                wishlistRepository
                        .existsByUserIdAndRacketId(
                                userId,
                                racketId
                        );

        return ResponseEntity.ok(
                isWishlisted
        );
    }
}