package com.example.racketbackend.services;

import com.example.racketbackend.models.Racket;
import com.example.racketbackend.models.RacketDnaProfile;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DnaBaselineFactory {

    public RacketDnaProfile create(
            Racket racket
    ) {
        RacketDnaProfile dna =
                new RacketDnaProfile();

        dna.setRacket(racket);
        dna.setVerified(false);
        dna.setDataOrigin(
                "SYSTEM_BASELINE"
        );

        dna.setCalibrationNotes(
                "Starter scores derived only from the existing " +
                        "SQL product text. Add verified specifications " +
                        "with the admin DNA API to raise confidence."
        );

        // Neutral starting DNA
        dna.setPower(60);
        dna.setSmash(60);
        dna.setSpeed(60);
        dna.setControl(60);
        dna.setPrecisionScore(60);
        dna.setDefense(60);
        dna.setDrive(60);
        dna.setNetPlay(60);
        dna.setClearShot(60);
        dna.setRepulsion(60);
        dna.setStability(60);
        dna.setTorsionResistance(58);
        dna.setForgiveness(62);
        dna.setVibrationDamping(58);
        dna.setComfort(60);
        dna.setEndurance(60);
        dna.setDurability(60);

        dna.setSinglesFit(60);
        dna.setDoublesFrontFit(60);
        dna.setDoublesRearFit(60);
        dna.setBeginnerFit(62);
        dna.setIntermediateFit(68);
        dna.setAdvancedFit(60);
        dna.setProfessionalFit(52);
        dna.setAttackFit(60);
        dna.setDefenseFit(60);
        dna.setAllRoundFit(65);
        dna.setWristComfort(60);
        dna.setStrengthDemand(52);
        dna.setLearningDifficulty(46);

        String searchable =
                (
                        (
                                racket.getTitle() == null
                                        ? ""
                                        : racket.getTitle()
                        ) +
                                " " +
                                (
                                        racket.getDescription() == null
                                                ? ""
                                                : racket.getDescription()
                                )
                ).toLowerCase(
                        Locale.ROOT
                );

        if (
                containsAny(
                        searchable,
                        "head-heavy",
                        "head heavy",
                        "power",
                        "smash",
                        "attack"
                )
        ) {
            dna.setPower(82);
            dna.setSmash(84);
            dna.setClearShot(78);
            dna.setAttackFit(86);
            dna.setDoublesRearFit(82);
            dna.setSinglesFit(74);
            dna.setStrengthDemand(70);
            dna.setSpeed(54);
            dna.setDefense(55);

            if (
                    containsAny(
                            searchable,
                            "head-heavy",
                            "head heavy"
                    )
            ) {
                dna.setBalanceType(
                        "HEAD_HEAVY"
                );
            }
        }

        if (
                containsAny(
                        searchable,
                        "head-light",
                        "head light",
                        "speed",
                        "fast",
                        "aerodynamic"
                )
        ) {
            dna.setSpeed(86);
            dna.setDrive(82);
            dna.setDefense(82);
            dna.setNetPlay(78);
            dna.setDoublesFrontFit(84);
            dna.setWristComfort(72);
            dna.setStrengthDemand(42);

            if (
                    containsAny(
                            searchable,
                            "head-light",
                            "head light"
                    )
            ) {
                dna.setBalanceType(
                        "HEAD_LIGHT"
                );
            }
        }

        if (
                containsAny(
                        searchable,
                        "even balance",
                        "all-round",
                        "all round",
                        "versatile"
                )
        ) {
            dna.setBalanceType("EVEN");
            dna.setAllRoundFit(86);
            dna.setControl(74);
            dna.setSpeed(72);
            dna.setPower(72);
        }

        if (
                containsAny(
                        searchable,
                        "control",
                        "precision",
                        "accurate"
                )
        ) {
            dna.setControl(86);
            dna.setPrecisionScore(84);
            dna.setStability(78);
            dna.setNetPlay(76);
            dna.setAllRoundFit(78);
        }

        if (
                containsAny(
                        searchable,
                        "stiff",
                        "extra stiff"
                )
        ) {
            dna.setShaftFlex(
                    searchable.contains(
                            "extra stiff"
                    )
                            ? "EXTRA_STIFF"
                            : "STIFF"
            );

            dna.setControl(
                    clamp(
                            dna.getControl() + 8
                    )
            );

            dna.setPrecisionScore(
                    clamp(
                            dna.getPrecisionScore() + 8
                    )
            );

            dna.setAdvancedFit(80);
            dna.setProfessionalFit(76);

            dna.setStrengthDemand(
                    clamp(
                            dna.getStrengthDemand() + 12
                    )
            );

            dna.setForgiveness(
                    clamp(
                            dna.getForgiveness() - 12
                    )
            );

            dna.setLearningDifficulty(
                    clamp(
                            dna.getLearningDifficulty() + 16
                    )
            );

        } else if (
                containsAny(
                        searchable,
                        "flexible",
                        "soft flex",
                        "medium flex"
                )
        ) {
            dna.setShaftFlex(
                    searchable.contains("medium")
                            ? "MEDIUM"
                            : "FLEXIBLE"
            );

            dna.setForgiveness(82);
            dna.setComfort(80);
            dna.setWristComfort(78);
            dna.setBeginnerFit(84);
            dna.setLearningDifficulty(28);
        }

        if (
                containsAny(
                        searchable,
                        "professional",
                        "tournament",
                        "pro"
                )
        ) {
            dna.setAdvancedFit(
                    clamp(
                            dna.getAdvancedFit() + 12
                    )
            );

            dna.setProfessionalFit(
                    clamp(
                            dna.getProfessionalFit() + 16
                    )
            );

            dna.setBeginnerFit(
                    clamp(
                            dna.getBeginnerFit() - 12
                    )
            );
        }

        return dna;
    }

    private boolean containsAny(
            String source,
            String... values
    ) {
        for (String value : values) {
            if (source.contains(value)) {
                return true;
            }
        }

        return false;
    }

    private int clamp(
            int value
    ) {
        return Math.max(
                0,
                Math.min(100, value)
        );
    }
}