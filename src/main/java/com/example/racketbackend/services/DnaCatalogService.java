package com.example.racketbackend.services;

import com.example.racketbackend.dto.*;
import com.example.racketbackend.models.*;
import com.example.racketbackend.repositories.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DnaCatalogService {

    private final RacketRepository
            racketRepository;

    private final RacketDnaProfileRepository
            profileRepository;

    private final BranchRepository
            branchRepository;

    private final BranchInventoryRepository
            inventoryRepository;

    private final DnaBaselineFactory
            baselineFactory;

    public DnaCatalogService(
            RacketRepository racketRepository,
            RacketDnaProfileRepository profileRepository,
            BranchRepository branchRepository,
            BranchInventoryRepository inventoryRepository,
            DnaBaselineFactory baselineFactory
    ) {
        this.racketRepository =
                racketRepository;

        this.profileRepository =
                profileRepository;

        this.branchRepository =
                branchRepository;

        this.inventoryRepository =
                inventoryRepository;

        this.baselineFactory =
                baselineFactory;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getBranches() {
        return branchRepository
                .findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toBranchResponse)
                .toList();
    }

    @Transactional
    public DnaRacketDetailsResponse getRacketDna(
            Integer racketId
    ) {
        Racket racket =
                requireRacket(racketId);

        RacketDnaProfile profile =
                ensureProfile(racket);

        DnaRacketDetailsResponse response =
                new DnaRacketDetailsResponse();

        response.setRacket(
                toRacketSummary(racket)
        );

        response.setDna(
                toProfileResponse(profile)
        );

        response.setAvailability(
                getAvailability(racket)
        );

        return response;
    }

    @Transactional
    public RacketDnaProfile upsertProfile(
            Integer racketId,
            RacketDnaProfile request
    ) {
        Racket racket =
                requireRacket(racketId);

        validateScores(request);

        RacketDnaProfile target =
                profileRepository
                        .findByRacket_Id(
                                racketId
                        )
                        .orElseGet(
                                RacketDnaProfile::new
                        );

        Integer existingId =
                target.getId();

        BeanUtils.copyProperties(
                request,
                target,
                "id",
                "racket",
                "updatedAt"
        );

        target.setId(existingId);
        target.setRacket(racket);

        if (target.getVerified() == null) {
            target.setVerified(false);
        }

        if (
                target.getDataOrigin() == null ||
                        target.getDataOrigin()
                                .isBlank()
        ) {
            target.setDataOrigin(
                    Boolean.TRUE.equals(
                            target.getVerified()
                    )
                            ? "VERIFIED"
                            : "ADMIN"
            );
        }

        return profileRepository.save(
                target
        );
    }

    @Transactional
    public BranchResponse saveBranch(
            Branch request
    ) {
        if (
                request.getCode() == null ||
                        request.getCode().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Branch code is required"
            );
        }

        if (
                request.getName() == null ||
                        request.getName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Branch name is required"
            );
        }

        Branch branch =
                branchRepository
                        .findByCodeIgnoreCase(
                                request
                                        .getCode()
                                        .trim()
                        )
                        .orElseGet(
                                Branch::new
                        );

        branch.setCode(
                request.getCode()
                        .trim()
                        .toUpperCase()
        );

        branch.setName(
                request.getName()
                        .trim()
        );

        branch.setAddress(
                request.getAddress()
        );

        branch.setActive(
                request.getActive() == null ||
                        request.getActive()
        );

        return toBranchResponse(
                branchRepository.save(
                        branch
                )
        );
    }

    @Transactional
    public BranchAvailabilityResponse updateInventory(
            Integer branchId,
            Integer racketId,
            BranchInventoryRequest request
    ) {
        Branch branch =
                branchRepository
                        .findById(branchId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Branch not found"
                                        )
                        );

        Racket racket =
                requireRacket(racketId);

        int stock =
                request.getStockQuantity() == null
                        ? 0
                        : request.getStockQuantity();

        int reserved =
                request.getReservedQuantity() == null
                        ? 0
                        : request.getReservedQuantity();

        if (
                stock < 0 ||
                        reserved < 0 ||
                        reserved > stock
        ) {
            throw new IllegalArgumentException(
                    "Stock must be non-negative and " +
                            "reserved quantity cannot exceed stock"
            );
        }

        BranchInventory inventory =
                inventoryRepository
                        .findByBranch_IdAndRacket_Id(
                                branchId,
                                racketId
                        )
                        .orElseGet(
                                BranchInventory::new
                        );

        inventory.setBranch(branch);
        inventory.setRacket(racket);
        inventory.setStockQuantity(stock);
        inventory.setReservedQuantity(reserved);

        return toAvailability(
                inventoryRepository.save(
                        inventory
                )
        );
    }

    @Transactional
    public RacketDnaProfile ensureProfile(
            Racket racket
    ) {
        return profileRepository
                .findByRacket_Id(
                        racket.getId()
                )
                .orElseGet(
                        () ->
                                profileRepository.save(
                                        baselineFactory.create(
                                                racket
                                        )
                                )
                );
    }

    @Transactional
    public List<BranchAvailabilityResponse>
    getAvailability(
            Racket racket
    ) {
        List<Branch> branches =
                branchRepository
                        .findByActiveTrueOrderByNameAsc();

        List<BranchInventory> current =
                new ArrayList<>(
                        inventoryRepository
                                .findByRacket_Id(
                                        racket.getId()
                                )
                );

        /*
         * For old rackets, the original global stock is
         * moved to the first branch only.
         * The other branches start at zero.
         */
        if (
                current.isEmpty() &&
                        !branches.isEmpty()
        ) {
            for (
                    int index = 0;
                    index < branches.size();
                    index++
            ) {
                BranchInventory inventory =
                        new BranchInventory();

                inventory.setBranch(
                        branches.get(index)
                );

                inventory.setRacket(
                        racket
                );

                inventory.setStockQuantity(
                        index == 0 &&
                                racket.getStock() != null
                                ? Math.max(
                                racket.getStock(),
                                0
                        )
                                : 0
                );

                inventory.setReservedQuantity(
                        0
                );

                current.add(
                        inventoryRepository.save(
                                inventory
                        )
                );
            }
        }

        List<BranchAvailabilityResponse>
                result =
                new ArrayList<>();

        for (Branch branch : branches) {
            BranchInventory inventory =
                    current.stream()
                            .filter(
                                    value ->
                                            value
                                                    .getBranch()
                                                    .getId()
                                                    .equals(
                                                            branch.getId()
                                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (inventory == null) {
                result.add(
                        new BranchAvailabilityResponse(
                                branch.getId(),
                                branch.getCode(),
                                branch.getName(),
                                0,
                                "NOT_AVAILABLE"
                        )
                );
            } else {
                result.add(
                        toAvailability(
                                inventory
                        )
                );
            }
        }

        result.sort(
                Comparator.comparing(
                        BranchAvailabilityResponse
                                ::getBranchName
                )
        );

        return result;
    }

    public DnaProfileResponse toProfileResponse(
            RacketDnaProfile profile
    ) {
        DnaProfileResponse response =
                new DnaProfileResponse();

        BeanUtils.copyProperties(
                profile,
                response
        );

        return response;
    }

    public RacketSummaryResponse toRacketSummary(
            Racket racket
    ) {
        return new RacketSummaryResponse(
                racket.getId(),
                racket.getTitle(),
                racket.getBrand(),
                racket.getDescription(),
                racket.getPrice(),
                racket.getStock() == null
                        ? 0
                        : racket.getStock(),
                racket.getImage()
        );
    }

    private Racket requireRacket(
            Integer racketId
    ) {
        return racketRepository
                .findById(racketId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Racket not found"
                                )
                );
    }

    private BranchResponse toBranchResponse(
            Branch branch
    ) {
        return new BranchResponse(
                branch.getId(),
                branch.getCode(),
                branch.getName(),
                branch.getAddress()
        );
    }

    private BranchAvailabilityResponse
    toAvailability(
            BranchInventory inventory
    ) {
        int available =
                inventory.getAvailableQuantity();

        String status;

        if (available <= 0) {
            status =
                    "NOT_AVAILABLE";
        } else if (available <= 3) {
            status =
                    "LOW_STOCK";
        } else {
            status =
                    "AVAILABLE";
        }

        return new BranchAvailabilityResponse(
                inventory
                        .getBranch()
                        .getId(),

                inventory
                        .getBranch()
                        .getCode(),

                inventory
                        .getBranch()
                        .getName(),

                available,
                status
        );
    }

    private void validateScores(
            RacketDnaProfile profile
    ) {
        Integer[] scores = {
                profile.getPower(),
                profile.getSmash(),
                profile.getSpeed(),
                profile.getControl(),
                profile.getPrecisionScore(),
                profile.getDefense(),
                profile.getDrive(),
                profile.getNetPlay(),
                profile.getClearShot(),
                profile.getRepulsion(),
                profile.getStability(),
                profile.getTorsionResistance(),
                profile.getForgiveness(),
                profile.getVibrationDamping(),
                profile.getComfort(),
                profile.getEndurance(),
                profile.getDurability(),
                profile.getSinglesFit(),
                profile.getDoublesFrontFit(),
                profile.getDoublesRearFit(),
                profile.getBeginnerFit(),
                profile.getIntermediateFit(),
                profile.getAdvancedFit(),
                profile.getProfessionalFit(),
                profile.getAttackFit(),
                profile.getDefenseFit(),
                profile.getAllRoundFit(),
                profile.getWristComfort(),
                profile.getStrengthDemand(),
                profile.getLearningDifficulty()
        };

        for (Integer score : scores) {
            if (
                    score != null &&
                            (
                                    score < 0 ||
                                            score > 100
                            )
            ) {
                throw new IllegalArgumentException(
                        "Every DNA score must be between 0 and 100"
                );
            }
        }
    }
}