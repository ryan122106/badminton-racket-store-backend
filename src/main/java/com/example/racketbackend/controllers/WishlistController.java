package com.example.racketbackend.controllers;

import com.example.racketbackend.models.User;
import com.example.racketbackend.services.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(
            WishlistService wishlistService
    ) {
        this.wishlistService =
                wishlistService;
    }

    @GetMapping("/wishlists/me")
    public ResponseEntity<Object> getMyWishlist(
            @AuthenticationPrincipal User user
    ) {

        return wishlistService.getMyWishlist(
                user.getId()
        );
    }

    @GetMapping("/wishlists/check/{racketId}")
    public ResponseEntity<Object> checkWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Integer racketId
    ) {

        return wishlistService.checkWishlist(
                user.getId(),
                racketId
        );
    }

    @PostMapping("/wishlists/{racketId}")
    public ResponseEntity<Object> addWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Integer racketId
    ) {

        return wishlistService.addWishlist(
                user.getId(),
                racketId
        );
    }

    @DeleteMapping("/wishlists/{racketId}")
    public ResponseEntity<Object> removeWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Integer racketId
    ) {

        return wishlistService.removeWishlist(
                user.getId(),
                racketId
        );
    }
}