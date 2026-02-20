package com.gamingplatform.ai;

import com.gamingplatform.entity.RubricDimension;

import java.util.Map;

public record EvaluationResult(
        Map<RubricDimension, Double> rubricScores,
        String feedback
) {
}
