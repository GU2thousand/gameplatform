package com.gamingplatform.service;

import com.gamingplatform.dto.UserProgressResponse;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.SalaryTier;
import com.gamingplatform.entity.Submission;
import com.gamingplatform.entity.SubmissionStatus;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProgressService {
    private final UserService users;
    private final SubmissionRepository submissions;
    private final EvaluationRepository evaluations;
    private final RecommendationService recommendations;

    public ProgressService(UserService users, SubmissionRepository submissions,
                           EvaluationRepository evaluations, RecommendationService recommendations) {
        this.users = users;
        this.submissions = submissions;
        this.evaluations = evaluations;
        this.recommendations = recommendations;
    }

    @Transactional(readOnly = true)
    public UserProgressResponse getProgress(Long userId) {
        UserProfile user = users.getById(userId);
        long totalAttempts = submissions.countByUser_Id(userId);
        long completedChallenges = submissions.countDistinctCompletedChallenges(userId);
        EvaluationRepository.EvaluationAverages projection = evaluations.findAveragesByUserId(userId);
        Double persistedAverage = projection == null ? null : projection.getAverageFinalScore();
        double average = valueOrZero(persistedAverage);
        SalaryTier tier = SalaryTier.fromScore(average);
        Map<RubricDimension, Double> dimensions = dimensionAverages(projection);
        RubricDimension weakest = persistedAverage == null ? null : weakest(dimensions);
        List<Submission> completed = submissions
                .findByUser_IdAndStatusOrderBySubmittedAtAsc(userId, SubmissionStatus.COMPLETED);
        List<Submission> allAttempts = submissions.findByUser_IdOrderBySubmittedAtDesc(userId);
        List<Evaluation> userEvaluations = evaluations.findBySubmission_User_Id(userId);
        Map<Long, Evaluation> bySubmission = new HashMap<>();
        userEvaluations.forEach(e -> bySubmission.put(e.getSubmission().getId(), e));
        Streaks streaks = streaks(completed);

        return new UserProgressResponse(user.getId(), user.getUsername(), user.getXp(), completedChallenges,
                totalAttempts, round2(average), tier, tier.getTitle(), recommendations.dimensionLabel(weakest),
                responseMap(dimensions), recommendations.recommendations(weakest), streaks.current(),
                streaks.longest(), dailyActivity(allAttempts, completed, bySubmission), trainingPlan(weakest, completed),
                scoreTrend(completed, bySubmission));
    }

    @Transactional(readOnly = true)
    public RubricDimension weakestDimension(Long userId) {
        EvaluationRepository.EvaluationAverages averages = evaluations.findAveragesByUserId(userId);
        if (averages == null || averages.getAverageFinalScore() == null) return null;
        return weakest(dimensionAverages(averages));
    }

    private List<UserProgressResponse.DailyActivity> dailyActivity(List<Submission> allAttempts,
                                                                    List<Submission> completed,
                                                                    Map<Long, Evaluation> bySubmission) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<UserProgressResponse.DailyActivity> result = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            List<Submission> attemptsOnDay = allAttempts.stream().filter(s -> day(s).equals(date)).toList();
            List<Submission> completedOnDay = completed.stream().filter(s -> day(s).equals(date)).toList();
            double average = completedOnDay.stream().map(bySubmission::get).filter(java.util.Objects::nonNull)
                    .mapToDouble(Evaluation::getFinalScore).average().orElse(0);
            result.add(new UserProgressResponse.DailyActivity(date, attemptsOnDay.size(), completedOnDay.size(), round2(average)));
        }
        return result;
    }

    private List<UserProgressResponse.TrainingPlanDay> trainingPlan(RubricDimension weakest,
                                                                     List<Submission> completed) {
        String base = recommendations.dimensionLabel(weakest);
        if (weakest == null) base = "Complete your first diagnostic challenge";
        List<String> focuses = List.of(base, "Deliberate practice", "Edge cases", "Tradeoffs",
                "Clear communication", "Timed retry", "Review progress");
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<UserProgressResponse.TrainingPlanDay> result = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            LocalDate planDate = today.plusDays(day - 1L);
            boolean done = completed.stream().anyMatch(s -> day(s).equals(planDate));
            result.add(new UserProgressResponse.TrainingPlanDay(day, focuses.get(day - 1), done));
        }
        return result;
    }

    private List<UserProgressResponse.ScoreTrendPoint> scoreTrend(List<Submission> completed,
                                                                   Map<Long, Evaluation> bySubmission) {
        return completed.stream().filter(s -> bySubmission.containsKey(s.getId()))
                .skip(Math.max(0, completed.size() - 20L))
                .map(s -> new UserProgressResponse.ScoreTrendPoint(s.getId(), s.getSubmittedAt(),
                        round2(bySubmission.get(s.getId()).getFinalScore())))
                .toList();
    }

    private Streaks streaks(List<Submission> completed) {
        Set<LocalDate> dates = new HashSet<>();
        completed.forEach(s -> dates.add(day(s)));
        List<LocalDate> sorted = dates.stream().sorted().toList();
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : sorted) {
            run = previous != null && date.equals(previous.plusDays(1)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = date;
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int current = 0;
        while (dates.contains(cursor)) { current++; cursor = cursor.minusDays(1); }
        return new Streaks(current, longest);
    }

    private LocalDate day(Submission submission) {
        return submission.getSubmittedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Map<RubricDimension, Double> dimensionAverages(EvaluationRepository.EvaluationAverages p) {
        Map<RubricDimension, Double> result = new EnumMap<>(RubricDimension.class);
        result.put(RubricDimension.REQUIREMENT_UNDERSTANDING, round2(valueOrZero(p == null ? null : p.getRequirementUnderstanding())));
        result.put(RubricDimension.LOGICAL_CLARITY, round2(valueOrZero(p == null ? null : p.getLogicalClarity())));
        result.put(RubricDimension.TECHNICAL_FEASIBILITY, round2(valueOrZero(p == null ? null : p.getTechnicalFeasibility())));
        result.put(RubricDimension.EDGE_CASE_COVERAGE, round2(valueOrZero(p == null ? null : p.getEdgeCaseCoverage())));
        result.put(RubricDimension.COMMUNICATION_STRUCTURE, round2(valueOrZero(p == null ? null : p.getCommunicationStructure())));
        return result;
    }

    private RubricDimension weakest(Map<RubricDimension, Double> values) {
        return values.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse(null);
    }

    private Map<String, Double> responseMap(Map<RubricDimension, Double> values) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("requirement_understanding", values.get(RubricDimension.REQUIREMENT_UNDERSTANDING));
        result.put("logical_clarity", values.get(RubricDimension.LOGICAL_CLARITY));
        result.put("technical_feasibility", values.get(RubricDimension.TECHNICAL_FEASIBILITY));
        result.put("edge_case_coverage", values.get(RubricDimension.EDGE_CASE_COVERAGE));
        result.put("communication_structure", values.get(RubricDimension.COMMUNICATION_STRUCTURE));
        return result;
    }

    private double valueOrZero(Double value) { return value == null ? 0 : value; }
    private double round2(double value) { return Math.round(value * 100.0) / 100.0; }
    private record Streaks(int current, int longest) {}
}
