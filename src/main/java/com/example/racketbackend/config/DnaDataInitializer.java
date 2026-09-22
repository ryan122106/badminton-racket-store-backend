package com.example.racketbackend.config;

import com.example.racketbackend.models.Branch;
import com.example.racketbackend.models.Racket;
import com.example.racketbackend.repositories.BranchRepository;
import com.example.racketbackend.repositories.RacketRepository;
import com.example.racketbackend.services.DnaCatalogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DnaDataInitializer
        implements CommandLineRunner {

    private final BranchRepository
            branchRepository;

    private final RacketRepository
            racketRepository;

    private final DnaCatalogService
            catalogService;

    public DnaDataInitializer(
            BranchRepository branchRepository,
            RacketRepository racketRepository,
            DnaCatalogService catalogService
    ) {
        this.branchRepository =
                branchRepository;

        this.racketRepository =
                racketRepository;

        this.catalogService =
                catalogService;
    }

    @Override
    @Transactional
    public void run(
            String... args
    ) {
        ensureBranch(
                "BR01",
                "ARC Central",
                "Replace with Branch 1 address"
        );

        ensureBranch(
                "BR02",
                "ARC North",
                "Replace with Branch 2 address"
        );

        ensureBranch(
                "BR03",
                "ARC South",
                "Replace with Branch 3 address"
        );

        List<Racket> rackets =
                racketRepository.findAll();

        for (Racket racket : rackets) {
            catalogService
                    .ensureProfile(racket);

            catalogService
                    .getAvailability(racket);
        }
    }

    private void ensureBranch(
            String code,
            String name,
            String address
    ) {
        if (
                branchRepository
                        .findByCodeIgnoreCase(
                                code
                        )
                        .isPresent()
        ) {
            return;
        }

        Branch branch =
                new Branch();

        branch.setCode(code);
        branch.setName(name);
        branch.setAddress(address);
        branch.setActive(true);

        branchRepository.save(
                branch
        );
    }
}