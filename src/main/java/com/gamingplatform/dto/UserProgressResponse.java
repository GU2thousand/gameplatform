package com.gamingplatform.dto;

import com.gamingplatform.entity.SalaryTier;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.Instant;

public record UserProgressResponse(
        Long userId,
        String username,
        int xp,
        long completedChallenges,
        long totalAttempts,
        double averageScore,
        SalaryTier salaryTier,
        String salaryTitle,
        String weakestDimension,
        Map<String, Double> dimensionAverages,
        List<String> recommendations,
        int currentStreak,
        int longestStreak,
        List<DailyActivity> last7Days,
        List<TrainingPlanDay> trainingPlan,
        List<ScoreTrendPoint> scoreTrend
) {
    public record DailyActivity(LocalDate date, long attempts, long completed, double averageScore) {}

    public record TrainingPlanDay(int day, String focus, boolean completed) {}

    public record ScoreTrendPoint(Long submissionId, Instant submittedAt, double score) {}
}
