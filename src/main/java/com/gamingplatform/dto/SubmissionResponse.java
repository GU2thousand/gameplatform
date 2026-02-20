package com.gamingplatform.dto;

import com.gamingplatform.entity.SalaryTier;

import java.time.Instant;
import java.util.Map;

public record SubmissionResponse(
        Long submissionId,
        Long evaluationId,
        double finalScore,
        SalaryTier salaryTier,
        String salaryTitle,
        Map<String, Double> rubricScores,
        String feedback,
        String improvementTrack,
        Instant submittedAt
) {
}
