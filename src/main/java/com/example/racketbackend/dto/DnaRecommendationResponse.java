package com.example.racketbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DnaRecommendationResponse {

    private String playerArchetype;
    private String summary;
    private String dataNote;
    private LocalDateTime generatedAt;

    private List<DnaMatchResponse> matches =
            new ArrayList<>();
}