package com.example.racketbackend.services;

import com.example.racketbackend.dto.*;
import com.example.racketbackend.models.Racket;
import com.example.racketbackend.models.RacketDnaProfile;
import com.example.racketbackend.repositories.RacketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class DnaRecommendationService {

    private final RacketRepository
            racketRepository;

    private final DnaCatalogService
            catalogService;

    public DnaRecommendationService(
            RacketRepository racketRepository,
            DnaCatalogService catalogService
    ) {
        this.racketRepository =
                racketRepository;

        this.catalogService =
                catalogService;
    }

    @Transactional
    public DnaRecommendationResponse recommend(
            DnaRecommendationRequest request
    ) {
        validateRequest(request);

        List<DnaMatchResponse> matches =
                new ArrayList<>();

        for (
                Racket racket :
                racketRepository.findAll()
        ) {
            RacketDnaProfile profile =
                    catalogService.ensureProfile(
                            racket
                    );

            List<BranchAvailabilityResponse>
                    availability =
                    catalogService.getAvailability(
                            racket
                    );

            BranchAvailabilityResponse
                    selectedAvailability =
                    chooseAvailability(
                            availability,
                            request.getBranchId()
                    );

            boolean available =
                    selectedAvailability != null &&
                            selectedAvailability
                                    .getAvailableQuantity() >
                                    0;

            if (
                    !Boolean.TRUE.equals(
                            request.getIncludeUnavailable()
                    ) &&
                            !available
            ) {
                continue;
            }

            List<Metric> metrics =
                    buildMetrics(
                            request,
                            profile
                    );

            double matchScore =
                    calculateMatch(
                            metrics
                    );

            matchScore +=
                    preferenceAdjustment(
                            request,
                            profile
                    );

            matchScore -=
                    budgetPenalty(
                            request,
                            racket
                    );

            if (available) {
                matchScore += 2.0;
            }

            matchScore =
                    roundOne(
                            clamp(
                                    matchScore,
                                    1.0,
                                    99.0
                            )
                    );

            DnaMatchResponse match =
                    new DnaMatchResponse();

            match.setMatchScore(
                    matchScore
            );

            match.setConfidenceScore(
                    calculateConfidence(
                            request,
                            profile
                    )
            );

            match.setRacket(
                    catalogService
                            .toRacketSummary(
                                    racket
                            )
            );

            match.setDna(
                    catalogService
                            .toProfileResponse(
                                    profile
                            )
            );

            match.setAvailability(
                    availability
            );

            match.setSelectedBranchAvailability(
                    selectedAvailability
            );

            match.setReasons(
                    buildReasons(
                            request,
                            profile,
                            selectedAvailability
                    )
            );

            match.setWatchOuts(
                    buildWatchOuts(
                            request,
                            profile,
                            racket,
                            selectedAvailability
                    )
            );

            matches.add(match);
        }

        matches.sort(
                Comparator
                        .comparingDouble(
                                DnaMatchResponse
                                        ::getMatchScore
                        )
                        .reversed()
                        .thenComparing(
                                match ->
                                        match
                                                .getRacket()
                                                .getPrice()
                        )
        );

        List<DnaMatchResponse> topMatches =
                matches.stream()
                        .limit(5)
                        .toList();

        for (
                int index = 0;
                index < topMatches.size();
                index++
        ) {
            topMatches
                    .get(index)
                    .setRank(index + 1);
        }

        DnaRecommendationResponse response =
                new DnaRecommendationResponse();

        response.setPlayerArchetype(
                buildArchetype(request)
        );

        response.setSummary(
                buildSummary(
                        request,
                        topMatches
                )
        );

        response.setDataNote(
                "Unknown manufacturer facts stay empty. " +
                        "SYSTEM_BASELINE results use existing catalogue " +
                        "text and have lower confidence until an admin verifies them."
        );

        response.setGeneratedAt(
                LocalDateTime.now()
        );

        response.setMatches(
                topMatches
        );

        return response;
    }

    private List<Metric> buildMetrics(
            DnaRecommendationRequest request,
            RacketDnaProfile profile
    ) {
        int powerTarget =
                priorityTarget(
                        request.getPowerPriority()
                );

        int speedTarget =
                average(
                        priorityTarget(
                                request.getSpeedPriority()
                        ),
                        bodyTarget(
                                request.getSwingSpeed()
                        )
                );

        int controlTarget =
                priorityTarget(
                        request.getControlPriority()
                );

        int comfortTarget =
                priorityTarget(
                        request.getComfortPriority()
                );

        int forgivenessTarget =
                priorityTarget(
                        request.getForgivenessPriority()
                );

        List<Metric> metrics =
                new ArrayList<>();

        add(
                metrics,
                profile.getPower(),
                powerTarget,
                priorityWeight(
                        request.getPowerPriority()
                )
        );

        add(
                metrics,
                profile.getSmash(),
                powerTarget,
                priorityWeight(
                        request.getPowerPriority()
                )
        );

        add(
                metrics,
                profile.getClearShot(),
                average(
                        powerTarget,
                        60
                ),
                1.2
        );

        add(
                metrics,
                profile.getSpeed(),
                speedTarget,
                priorityWeight(
                        request.getSpeedPriority()
                )
        );

        add(
                metrics,
                profile.getDrive(),
                speedTarget,
                priorityWeight(
                        request.getSpeedPriority()
                )
        );

        add(
                metrics,
                profile.getControl(),
                controlTarget,
                priorityWeight(
                        request.getControlPriority()
                )
        );

        add(
                metrics,
                profile.getPrecisionScore(),
                controlTarget,
                priorityWeight(
                        request.getControlPriority()
                )
        );

        add(
                metrics,
                profile.getStability(),
                controlTarget,
                1.5
        );

        add(
                metrics,
                profile.getForgiveness(),
                forgivenessTarget,
                priorityWeight(
                        request.getForgivenessPriority()
                )
        );

        add(
                metrics,
                profile.getComfort(),
                comfortTarget,
                priorityWeight(
                        request.getComfortPriority()
                )
        );

        add(
                metrics,
                profile.getWristComfort(),
                wristTarget(request),
                2.0
        );

        add(
                metrics,
                profile.getEndurance(),
                bodyTarget(
                        request.getEndurance()
                ),
                1.4
        );

        add(
                metrics,
                profile.getStrengthDemand(),
                bodyTarget(
                        request.getStrength()
                ),
                1.8
        );

        add(
                metrics,
                profile.getLearningDifficulty(),
                learningTarget(request),
                1.6
        );

        add(
                metrics,
                skillFit(
                        profile,
                        request.getSkillLevel()
                ),
                90,
                2.6
        );

        add(
                metrics,
                styleFit(
                        profile,
                        request.getPlayStyle()
                ),
                92,
                3.0
        );

        add(
                metrics,
                gameFit(
                        profile,
                        request
                ),
                90,
                2.5
        );

        return metrics;
    }

    private void add(
            List<Metric> metrics,
            Integer actual,
            int target,
            double weight
    ) {
        if (actual != null) {
            metrics.add(
                    new Metric(
                            actual,
                            target,
                            weight
                    )
            );
        }
    }

    private double calculateMatch(
            List<Metric> metrics
    ) {
        if (metrics.isEmpty()) {
            return 35.0;
        }

        double earned = 0.0;
        double totalWeight = 0.0;

        for (Metric metric : metrics) {
            double similarity =
                    100.0 -
                            Math.abs(
                                    metric.actual() -
                                            metric.target()
                            );

            earned +=
                    Math.max(
                            similarity,
                            0.0
                    ) *
                            metric.weight();

            totalWeight +=
                    metric.weight();
        }

        return earned /
                totalWeight;
    }

    private double preferenceAdjustment(
            DnaRecommendationRequest request,
            RacketDnaProfile profile
    ) {
        return comparePreference(
                request.getPreferredWeightClass(),
                profile.getWeightClass()
        ) +
                comparePreference(
                        request.getPreferredBalanceType(),
                        profile.getBalanceType()
                ) +
                comparePreference(
                        request.getPreferredShaftFlex(),
                        profile.getShaftFlex()
                );
    }

    private double comparePreference(
            String preferred,
            String actual
    ) {
        if (
                isBlank(preferred) ||
                        "NO_PREFERENCE".equals(
                                normalize(preferred)
                        ) ||
                        isBlank(actual)
        ) {
            return 0.0;
        }

        return normalize(preferred)
                .equals(
                        normalize(actual)
                )
                ? 2.0
                : -1.0;
    }

    private double budgetPenalty(
            DnaRecommendationRequest request,
            Racket racket
    ) {
        if (
                request.getBudgetMax() == null ||
                        request.getBudgetMax() <= 0 ||
                        racket.getPrice() == null ||
                        racket.getPrice() <=
                                request.getBudgetMax()
        ) {
            return 0.0;
        }

        double percentageOver =
                (
                        racket.getPrice() -
                                request.getBudgetMax()
                ) /
                        request.getBudgetMax();

        return Math.min(
                18.0,
                percentageOver * 30.0
        );
    }

    private int calculateConfidence(
            DnaRecommendationRequest request,
            RacketDnaProfile profile
    ) {
        int base;

        if (
                Boolean.TRUE.equals(
                        profile.getVerified()
                ) ||
                        "VERIFIED".equals(
                                normalize(
                                        profile.getDataOrigin()
                                )
                        )
        ) {
            base = 84;

        } else if (
                "ADMIN".equals(
                        normalize(
                                profile.getDataOrigin()
                        )
                )
        ) {
            base = 67;
        } else {
            base = 47;
        }

        int factCount =
                countPresent(
                        profile.getWeightClass(),
                        profile.getWeightGrams(),
                        profile.getGripSize(),
                        profile.getBalanceType(),
                        profile.getBalancePointMm(),
                        profile.getShaftFlex(),
                        profile.getShaftThicknessMm(),
                        profile.getLengthMm(),
                        profile.getMinTensionLbs(),
                        profile.getMaxTensionLbs(),
                        profile.getFrameShape(),
                        profile.getFrameMaterial(),
                        profile.getShaftMaterial(),
                        profile.getJointType(),
                        profile.getTechnologies()
                );

        int answerCount =
                countPresent(
                        request.getSkillLevel(),
                        request.getGameType(),
                        request.getCourtRole(),
                        request.getPlayStyle(),
                        request.getPowerPriority(),
                        request.getSpeedPriority(),
                        request.getControlPriority(),
                        request.getComfortPriority(),
                        request.getForgivenessPriority(),
                        request.getStrength(),
                        request.getSwingSpeed(),
                        request.getEndurance(),
                        request.getWristSensitivity()
                );

        return (int) clamp(
                base +
                        Math.round(
                                factCount /
                                        15.0 *
                                        9.0
                        ) +
                        Math.round(
                                answerCount /
                                        13.0 *
                                        5.0
                        ),
                20,
                98
        );
    }

    private List<String> buildReasons(
            DnaRecommendationRequest request,
            RacketDnaProfile profile,
            BranchAvailabilityResponse availability
    ) {
        List<String> reasons =
                new ArrayList<>();

        String style =
                normalize(
                        request.getPlayStyle()
                );

        switch (style) {
            case "ATTACK" ->
                    reasons.add(
                            scoreReason(
                                    "Smash",
                                    profile.getSmash(),
                                    "for your attacking game"
                            )
                    );

            case "DEFENSE" ->
                    reasons.add(
                            scoreReason(
                                    "Defense",
                                    profile.getDefense(),
                                    "for fast recovery"
                            )
                    );

            case "CONTROL" ->
                    reasons.add(
                            scoreReason(
                                    "Control",
                                    profile.getControl(),
                                    "for placement and precision"
                            )
                    );

            case "SPEED" ->
                    reasons.add(
                            scoreReason(
                                    "Speed",
                                    profile.getSpeed(),
                                    "for quick exchanges"
                            )
                    );

            default ->
                    reasons.add(
                            scoreReason(
                                    "All-round fit",
                                    profile.getAllRoundFit(),
                                    "for a balanced game"
                            )
                    );
        }

        reasons.add(
                scoreReason(
                        "Skill fit",
                        skillFit(
                                profile,
                                request.getSkillLevel()
                        ),
                        "at your current level"
                )
        );

        if (
                availability != null &&
                        availability
                                .getAvailableQuantity() >
                                0
        ) {
            reasons.add(
                    availability
                            .getAvailableQuantity() +
                            " ready at " +
                            availability
                                    .getBranchName()
            );
        } else {
            reasons.add(
                    "DNA match is shown even though " +
                            "the selected branch has no stock"
            );
        }

        return reasons;
    }

    private List<String> buildWatchOuts(
            DnaRecommendationRequest request,
            RacketDnaProfile profile,
            Racket racket,
            BranchAvailabilityResponse availability
    ) {
        List<String> warnings =
                new ArrayList<>();

        if (
                profile.getStrengthDemand() != null &&
                        profile.getStrengthDemand() >= 72 &&
                        value(
                                request.getStrength(),
                                3
                        ) <= 2
        ) {
            warnings.add(
                    "High strength demand may become tiring"
            );
        }

        if (
                profile.getWristComfort() != null &&
                        profile.getWristComfort() < 58 &&
                        value(
                                request.getWristSensitivity(),
                                3
                        ) >= 4
        ) {
            warnings.add(
                    "Lower wrist-comfort score than your preference"
            );
        }

        if (
                request.getBudgetMax() != null &&
                        racket.getPrice() != null &&
                        racket.getPrice() >
                                request.getBudgetMax()
        ) {
            warnings.add(
                    "Above your RM %.2f budget"
                            .formatted(
                                    request.getBudgetMax()
                            )
            );
        }

        if (
                availability == null ||
                        availability
                                .getAvailableQuantity() <=
                                0
        ) {
            warnings.add(
                    "Not available at the selected branch"
            );
        }

        if (
                !Boolean.TRUE.equals(
                        profile.getVerified()
                )
        ) {
            warnings.add(
                    "Baseline DNA: admin verification will improve confidence"
            );
        }

        return warnings;
    }

    private String scoreReason(
            String label,
            Integer score,
            String suffix
    ) {
        return score == null
                ? label +
                " data is not available yet"
                : label +
                " DNA " +
                score +
                "/100 " +
                suffix;
    }

    private BranchAvailabilityResponse chooseAvailability(
            List<BranchAvailabilityResponse> availability,
            Integer branchId
    ) {
        if (availability.isEmpty()) {
            return null;
        }

        if (branchId != null) {
            return availability
                    .stream()
                    .filter(
                            value ->
                                    branchId.equals(
                                            value.getBranchId()
                                    )
                    )
                    .findFirst()
                    .orElse(null);
        }

        return availability
                .stream()
                .max(
                        Comparator.comparingInt(
                                BranchAvailabilityResponse
                                        ::getAvailableQuantity
                        )
                )
                .orElse(
                        availability.get(0)
                );
    }

    private String buildArchetype(
            DnaRecommendationRequest request
    ) {
        String style =
                normalize(
                        request.getPlayStyle()
                );

        String role =
                normalize(
                        request.getCourtRole()
                );

        if (
                "ATTACK".equals(style) &&
                        "REAR".equals(role)
        ) {
            return "Explosive Rear-Court Striker";
        }

        if (
                "SPEED".equals(style) &&
                        "FRONT".equals(role)
        ) {
            return "Lightning Front-Court Hunter";
        }

        return switch (style) {
            case "CONTROL" ->
                    "Precision Rally Architect";

            case "DEFENSE" ->
                    "Counter-Attack Guardian";

            case "ATTACK" ->
                    "Power Point Finisher";

            case "SPEED" ->
                    "Rapid Exchange Specialist";

            default ->
                    "Adaptive All-Court Player";
        };
    }

    private String buildSummary(
            DnaRecommendationRequest request,
            List<DnaMatchResponse> matches
    ) {
        if (matches.isEmpty()) {
            return "No racket matches the current stock filter.";
        }

        DnaMatchResponse best =
                matches.get(0);

        return "%s leads with a %.1f%% match for your %s profile."
                .formatted(
                        best.getRacket()
                                .getTitle(),

                        best.getMatchScore(),

                        display(
                                request.getPlayStyle(),
                                "all-round"
                        )
                );
    }

    private int skillFit(
            RacketDnaProfile profile,
            String level
    ) {
        return switch (
                normalize(level)
                ) {
            case "BEGINNER" ->
                    value(
                            profile.getBeginnerFit(),
                            50
                    );

            case "ADVANCED" ->
                    value(
                            profile.getAdvancedFit(),
                            50
                    );

            case "PROFESSIONAL",
                    "PRO" ->
                    value(
                            profile.getProfessionalFit(),
                            50
                    );

            default ->
                    value(
                            profile.getIntermediateFit(),
                            50
                    );
        };
    }

    private int styleFit(
            RacketDnaProfile profile,
            String style
    ) {
        return switch (
                normalize(style)
                ) {
            case "ATTACK" ->
                    value(
                            profile.getAttackFit(),
                            50
                    );

            case "DEFENSE" ->
                    value(
                            profile.getDefenseFit(),
                            50
                    );

            case "CONTROL" ->
                    average(
                            value(
                                    profile.getControl(),
                                    50
                            ),
                            value(
                                    profile.getPrecisionScore(),
                                    50
                            )
                    );

            case "SPEED" ->
                    average(
                            value(
                                    profile.getSpeed(),
                                    50
                            ),
                            value(
                                    profile.getDrive(),
                                    50
                            )
                    );

            default ->
                    value(
                            profile.getAllRoundFit(),
                            50
                    );
        };
    }

    private int gameFit(
            RacketDnaProfile profile,
            DnaRecommendationRequest request
    ) {
        String type =
                normalize(
                        request.getGameType()
                );

        String role =
                normalize(
                        request.getCourtRole()
                );

        if ("SINGLES".equals(type)) {
            return value(
                    profile.getSinglesFit(),
                    50
            );
        }

        if ("FRONT".equals(role)) {
            return value(
                    profile.getDoublesFrontFit(),
                    50
            );
        }

        if ("REAR".equals(role)) {
            return value(
                    profile.getDoublesRearFit(),
                    50
            );
        }

        return average(
                value(
                        profile.getDoublesFrontFit(),
                        50
                ),
                value(
                        profile.getDoublesRearFit(),
                        50
                )
        );
    }

    private int wristTarget(
            DnaRecommendationRequest request
    ) {
        return 55 +
                value(
                        request.getWristSensitivity(),
                        3
                ) *
                        8;
    }

    private int learningTarget(
            DnaRecommendationRequest request
    ) {
        return switch (
                normalize(
                        request.getSkillLevel()
                )
                ) {
            case "BEGINNER" -> 25;
            case "ADVANCED" -> 72;
            case "PROFESSIONAL",
                    "PRO" -> 88;
            default -> 50;
        };
    }

    private int priorityTarget(
            Integer priority
    ) {
        return 42 +
                value(
                        priority,
                        3
                ) *
                        10;
    }

    private double priorityWeight(
            Integer priority
    ) {
        return 1.0 +
                value(
                        priority,
                        3
                ) *
                        0.55;
    }

    private int bodyTarget(
            Integer bodyValue
    ) {
        return 30 +
                value(
                        bodyValue,
                        3
                ) *
                        12;
    }

    private int average(
            int first,
            int second
    ) {
        return (
                first + second
        ) /
                2;
    }

    private void validateRequest(
            DnaRecommendationRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "DNA questionnaire is required"
            );
        }

        Integer[] sliders = {
                request.getPowerPriority(),
                request.getSpeedPriority(),
                request.getControlPriority(),
                request.getComfortPriority(),
                request.getForgivenessPriority(),
                request.getStrength(),
                request.getSwingSpeed(),
                request.getEndurance(),
                request.getWristSensitivity()
        };

        for (Integer slider : sliders) {
            if (
                    slider != null &&
                            (
                                    slider < 1 ||
                                            slider > 5
                            )
            ) {
                throw new IllegalArgumentException(
                        "Questionnaire values must be between 1 and 5"
                );
            }
        }

        if (
                request.getBudgetMax() != null &&
                        request.getBudgetMax() < 0
        ) {
            throw new IllegalArgumentException(
                    "Budget cannot be negative"
            );
        }
    }

    private int countPresent(
            Object... values
    ) {
        int count = 0;

        for (Object item : values) {
            if (
                    item != null &&
                            (
                                    !(item instanceof String text) ||
                                            !text.isBlank()
                            )
            ) {
                count++;
            }
        }

        return count;
    }

    private static int value(
            Integer value,
            int fallback
    ) {
        return value == null
                ? fallback
                : value;
    }

    private String normalize(
            String value
    ) {
        return value == null
                ? ""
                : value
                .trim()
                .toUpperCase(
                        Locale.ROOT
                )
                .replace(
                        '-',
                        '_'
                )
                .replace(
                        ' ',
                        '_'
                );
    }

    private boolean isBlank(
            String value
    ) {
        return value == null ||
                value.isBlank();
    }

    private String display(
            String value,
            String fallback
    ) {
        if (isBlank(value)) {
            return fallback;
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                )
                .replace(
                        '_',
                        '-'
                );
    }

    private double roundOne(
            double value
    ) {
        return Math.round(
                value * 10.0
        ) /
                10.0;
    }

    private double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }

    private record Metric(
            int actual,
            int target,
            double weight
    ) {
    }
}