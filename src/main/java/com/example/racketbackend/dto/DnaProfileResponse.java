package com.example.racketbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DnaProfileResponse {

    private String weightClass;
    private Double weightGrams;
    private String gripSize;
    private String balanceType;
    private Double balancePointMm;
    private String shaftFlex;
    private Double shaftThicknessMm;
    private Double lengthMm;
    private Double minTensionLbs;
    private Double maxTensionLbs;
    private String frameShape;
    private String frameMaterial;
    private String shaftMaterial;
    private String jointType;
    private String color;
    private String countryOfOrigin;
    private String recommendedString;
    private String technologies;
    private String sourceUrl;
    private String calibrationNotes;
    private Boolean verified;
    private String dataOrigin;

    private Integer power;
    private Integer smash;
    private Integer speed;
    private Integer control;
    private Integer precisionScore;
    private Integer defense;
    private Integer drive;
    private Integer netPlay;
    private Integer clearShot;
    private Integer repulsion;
    private Integer stability;
    private Integer torsionResistance;
    private Integer forgiveness;
    private Integer vibrationDamping;
    private Integer comfort;
    private Integer endurance;
    private Integer durability;

    private Integer singlesFit;
    private Integer doublesFrontFit;
    private Integer doublesRearFit;
    private Integer beginnerFit;
    private Integer intermediateFit;
    private Integer advancedFit;
    private Integer professionalFit;
    private Integer attackFit;
    private Integer defenseFit;
    private Integer allRoundFit;
    private Integer wristComfort;
    private Integer strengthDemand;
    private Integer learningDifficulty;

    private LocalDateTime updatedAt;
}