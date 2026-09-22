package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository
        extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByUserId(
            Integer userId
    );

    Optional<Wishlist> findByUserIdAndRacketId(
            Integer userId,
            Integer racketId
    );

    boolean existsByUserIdAndRacketId(
            Integer userId,
            Integer racketId
    );
}