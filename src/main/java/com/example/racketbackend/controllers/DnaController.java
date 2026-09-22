package com.example.racketbackend.controllers;

import com.example.racketbackend.dto.*;
import com.example.racketbackend.services.DnaCatalogService;
import com.example.racketbackend.services.DnaRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DnaController {

    private final DnaCatalogService
            catalogService;

    private final DnaRecommendationService
            recommendationService;

    public DnaController(
            DnaCatalogService catalogService,
            DnaRecommendationService recommendationService
    ) {
        this.catalogService =
                catalogService;

        this.recommendationService =
                recommendationService;
    }

    @GetMapping("/branches")
    public List<BranchResponse> getBranches() {
        return catalogService
                .getBranches();
    }

    @GetMapping("/dna/rackets/{racketId}")
    public DnaRacketDetailsResponse getRacketDna(
            @PathVariable
            Integer racketId
    ) {
        return catalogService
                .getRacketDna(
                        racketId
                );
    }

    @PostMapping("/dna/recommendations")
    public DnaRecommendationResponse recommend(
            @RequestBody
            DnaRecommendationRequest request
    ) {
        return recommendationService
                .recommend(request);
    }
}