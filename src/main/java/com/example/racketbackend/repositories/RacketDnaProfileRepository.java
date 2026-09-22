package com.example.racketbackend.repositories;

import com.example.racketbackend.models.RacketDnaProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RacketDnaProfileRepository
        extends JpaRepository<RacketDnaProfile, Integer> {

    Optional<RacketDnaProfile> findByRacket_Id(
            Integer racketId
    );
}