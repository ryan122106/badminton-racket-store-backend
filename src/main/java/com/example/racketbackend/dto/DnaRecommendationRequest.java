package com.example.racketbackend.dto;

import lombok.Data;

@Data
public class DnaRecommendationRequest {

    private String skillLevel;
    private String gameType;
    private String courtRole;
    private String playStyle;

    // Values from 1–5
    private Integer powerPriority;
    private Integer speedPriority;
    private Integer controlPriority;
    private Integer comfortPriority;
    private Integer forgivenessPriority;

    private Integer strength;
    private Integer swingSpeed;
    private Integer endurance;
    private Integer wristSensitivity;

    private String preferredWeightClass;
    private String preferredBalanceType;
    private String preferredShaftFlex;

    private Double budgetMax;
    private Integer branchId;

    private Boolean includeUnavailable = true;
}