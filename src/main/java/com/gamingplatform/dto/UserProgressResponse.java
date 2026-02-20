package com.gamingplatform.dto;

import com.gamingplatform.entity.SalaryTier;

import java.util.List;
import java.util.Map;

public record UserProgressResponse(
        Long userId,
        String username,
        int xp,
        long completedChallenges,
        double averageScore,
        SalaryTier salaryTier,
        String salaryTitle,
        String weakestDimension,
        Map<String, Double> dimensionAverages,
        List<String> recommendations
) {
}
