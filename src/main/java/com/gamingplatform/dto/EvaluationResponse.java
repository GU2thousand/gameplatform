package com.gamingplatform.dto;

import com.gamingplatform.entity.SalaryTier;

import java.util.List;
import java.util.Map;

public record EvaluationResponse(
        Long evaluationId,
        double finalScore,
        SalaryTier skillTier,
        String skillTitle,
        Map<String, Double> rubricScores,
        Map<String, Double> rubricWeights,
        String feedback,
        List<String> strengths,
        List<String> improvements,
        String exampleOutline,
        String improvementTrack,
        String provider
) {}
