package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository
        extends JpaRepository<Branch, Integer> {

    List<Branch> findByActiveTrueOrderByNameAsc();

    Optional<Branch> findByCodeIgnoreCase(
            String code
    );
}