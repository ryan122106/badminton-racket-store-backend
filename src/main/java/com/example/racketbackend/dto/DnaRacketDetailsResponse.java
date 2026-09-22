package com.example.racketbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DnaRacketDetailsResponse {

    private RacketSummaryResponse racket;
    private DnaProfileResponse dna;

    private List<BranchAvailabilityResponse>
            availability =
            new ArrayList<>();
}