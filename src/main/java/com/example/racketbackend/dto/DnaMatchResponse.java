package com.example.racketbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DnaMatchResponse {

    private Integer rank;
    private Double matchScore;
    private Integer confidenceScore;

    private RacketSummaryResponse racket;
    private DnaProfileResponse dna;

    private List<String> reasons =
            new ArrayList<>();

    private List<String> watchOuts =
            new ArrayList<>();

    private List<BranchAvailabilityResponse>
            availability =
            new ArrayList<>();

    private BranchAvailabilityResponse
            selectedBranchAvailability;
}