package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByRacketId(Integer racketId);

    List<Review> findByUserId(Integer userId);

    boolean existsByUserIdAndRacketId(
            Integer userId,
            Integer racketId
    );

}