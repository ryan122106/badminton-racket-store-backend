package com.example.racketbackend.repositories;

import com.example.racketbackend.models.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchInventoryRepository
        extends JpaRepository<BranchInventory, Integer> {

    List<BranchInventory> findByRacket_Id(
            Integer racketId
    );

    Optional<BranchInventory>
    findByBranch_IdAndRacket_Id(
            Integer branchId,
            Integer racketId
    );
}