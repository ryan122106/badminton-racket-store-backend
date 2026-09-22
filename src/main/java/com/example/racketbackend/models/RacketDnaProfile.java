package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "RacketDnaProfile")
public class RacketDnaProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "racket_id",
            nullable = false,
            unique = true
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Racket racket;

    // Real manufacturer specifications.
    // Null means the information is not available.
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

    @Column(length = 2000)
    private String technologies;

    @Column(length = 1000)
    private String sourceUrl;

    @Column(length = 1000)
    private String calibrationNotes;

    private Boolean verified = false;
    private String dataOrigin = "ADMIN";

    // On-court performance: 0–100
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

    // Player and tactical fit: 0–100
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

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}