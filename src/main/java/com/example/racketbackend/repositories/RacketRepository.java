package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Racket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface RacketRepository extends JpaRepository<Racket, Integer> {

    List<Racket> findByBrand(String brand);

    List<Racket> findByTitleContaining(String title);

}