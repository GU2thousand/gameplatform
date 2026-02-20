package com.gamingplatform.service;

import com.gamingplatform.dto.UserProgressResponse;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.SalaryTier;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressService {

    private final UserService userService;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final RecommendationService recommendationService;

    public ProgressService(
            UserService userService,
            SubmissionRepository submissionRepository,
            EvaluationRepository evaluationRepository,
            RecommendationService recommendationService
    ) {
        this.userService = userService;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
        this.recommendationService = recommendationService;
    }

    @Transactional(readOnly = true)
    public UserProgressResponse getProgress(Long userId) {
        UserProfile user = userService.getById(userId);
        long completed = submissionRepository.countByUser_Id(userId);

        Double average = evaluationRepository.findAverageFinalScoreByUserId(userId);
        double averageScore = average == null ? 0 : average;

        SalaryTier tier = SalaryTier.fromScore(averageScore);
        List<Evaluation> evaluations = evaluationRepository.findAllByUserId(userId);

        Map<RubricDimension, Double> dimensionAverages = aggregateDimensionAverages(evaluations);
        RubricDimension weakestDimension = evaluations.isEmpty() ? null : findWeakestDimension(dimensionAverages);

        return new UserProgressResponse(
                user.getId(),
                user.getUsername(),
                user.getXp(),
                completed,
                round2(averageScore),
                tier,
                tier.getTitle(),
                recommendationService.dimensionLabel(weakestDimension),
                toResponseMap(dimensionAverages),
                recommendationService.recommendations(weakestDimension)
        );
    }

    private Map<RubricDimension, Double> aggregateDimensionAverages(List<Evaluation> evaluations) {
        Map<RubricDimension, Double> sums = new EnumMap<>(RubricDimension.class);
        for (RubricDimension dimension : RubricDimension.values()) {
            sums.put(dimension, 0.0);
        }

        if (evaluations.isEmpty()) {
            return sums;
        }

        for (Evaluation evaluation : evaluations) {
            sums.computeIfPresent(RubricDimension.REQUIREMENT_UNDERSTANDING,
                    (d, v) -> v + evaluation.getRequirementUnderstanding());
            sums.computeIfPresent(RubricDimension.LOGICAL_CLARITY,
                    (d, v) -> v + evaluation.getLogicalClarity());
            sums.computeIfPresent(RubricDimension.TECHNICAL_FEASIBILITY,
                    (d, v) -> v + evaluation.getTechnicalFeasibility());
            sums.computeIfPresent(RubricDimension.EDGE_CASE_COVERAGE,
                    (d, v) -> v + evaluation.getEdgeCaseCoverage());
            sums.computeIfPresent(RubricDimension.COMMUNICATION_STRUCTURE,
                    (d, v) -> v + evaluation.getCommunicationStructure());
        }

        Map<RubricDimension, Double> averages = new EnumMap<>(RubricDimension.class);
        for (Map.Entry<RubricDimension, Double> entry : sums.entrySet()) {
            averages.put(entry.getKey(), round2(entry.getValue() / evaluations.size()));
        }
        return averages;
    }

    private RubricDimension findWeakestDimension(Map<RubricDimension, Double> averages) {
        return averages.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Map<String, Double> toResponseMap(Map<RubricDimension, Double> averages) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("requirement_understanding", averages.get(RubricDimension.REQUIREMENT_UNDERSTANDING));
        result.put("logical_clarity", averages.get(RubricDimension.LOGICAL_CLARITY));
        result.put("technical_feasibility", averages.get(RubricDimension.TECHNICAL_FEASIBILITY));
        result.put("edge_case_coverage", averages.get(RubricDimension.EDGE_CASE_COVERAGE));
        result.put("communication_structure", averages.get(RubricDimension.COMMUNICATION_STRUCTURE));
        return result;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
