package com.gamingplatform.service;

import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.dto.SubmissionRequest;
import com.gamingplatform.dto.SubmissionResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.SalaryTier;
import com.gamingplatform.entity.Submission;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.exception.InvalidAiOutputException;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SubmissionService {

    private static final Map<RubricDimension, Double> WEIGHTS = Map.of(
            RubricDimension.REQUIREMENT_UNDERSTANDING, 0.25,
            RubricDimension.LOGICAL_CLARITY, 0.20,
            RubricDimension.TECHNICAL_FEASIBILITY, 0.25,
            RubricDimension.EDGE_CASE_COVERAGE, 0.15,
            RubricDimension.COMMUNICATION_STRUCTURE, 0.15
    );

    private final UserService userService;
    private final ChallengeService challengeService;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserProfileRepository userProfileRepository;
    private final EvaluationAiClient evaluationAiClient;
    private final RecommendationService recommendationService;

    public SubmissionService(
            UserService userService,
            ChallengeService challengeService,
            SubmissionRepository submissionRepository,
            EvaluationRepository evaluationRepository,
            UserProfileRepository userProfileRepository,
            EvaluationAiClient evaluationAiClient,
            RecommendationService recommendationService
    ) {
        this.userService = userService;
        this.challengeService = challengeService;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
        this.userProfileRepository = userProfileRepository;
        this.evaluationAiClient = evaluationAiClient;
        this.recommendationService = recommendationService;
    }

    @Transactional
    public SubmissionResponse submit(SubmissionRequest request) {
        UserProfile user = userService.getById(request.getUserId());
        Challenge challenge = challengeService.getById(request.getChallengeId());

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setChallenge(challenge);
        submission.setAnswer(request.getAnswer());
        submission = submissionRepository.save(submission);

        EvaluationResult evaluationResult = evaluationAiClient.evaluate(challenge, request.getAnswer());
        Map<RubricDimension, Double> rubricScores = normalizeScores(evaluationResult.rubricScores());

        double finalScore = weightedFinalScore(rubricScores);

        Evaluation evaluation = new Evaluation();
        evaluation.setSubmission(submission);
        evaluation.setRequirementUnderstanding(rubricScores.get(RubricDimension.REQUIREMENT_UNDERSTANDING));
        evaluation.setLogicalClarity(rubricScores.get(RubricDimension.LOGICAL_CLARITY));
        evaluation.setTechnicalFeasibility(rubricScores.get(RubricDimension.TECHNICAL_FEASIBILITY));
        evaluation.setEdgeCaseCoverage(rubricScores.get(RubricDimension.EDGE_CASE_COVERAGE));
        evaluation.setCommunicationStructure(rubricScores.get(RubricDimension.COMMUNICATION_STRUCTURE));
        evaluation.setFinalScore(finalScore);
        evaluation.setFeedback(evaluationResult.feedback());
        evaluation = evaluationRepository.save(evaluation);

        user.addXp((int) Math.round(finalScore));
        userProfileRepository.save(user);

        double averageScore = averageScore(user.getId());
        SalaryTier salaryTier = SalaryTier.fromScore(averageScore);
        String improvementTrack = recommendationService.buildImprovementTrack(rubricScores);

        return new SubmissionResponse(
                submission.getId(),
                evaluation.getId(),
                round2(finalScore),
                salaryTier,
                salaryTier.getTitle(),
                toResponseScores(rubricScores),
                evaluationResult.feedback(),
                improvementTrack,
                submission.getSubmittedAt()
        );
    }

    private double weightedFinalScore(Map<RubricDimension, Double> rubricScores) {
        double score = 0;
        for (Map.Entry<RubricDimension, Double> weight : WEIGHTS.entrySet()) {
            score += weight.getValue() * rubricScores.get(weight.getKey());
        }
        return round2(score);
    }

    private Map<RubricDimension, Double> normalizeScores(Map<RubricDimension, Double> scores) {
        if (scores == null) {
            throw new InvalidAiOutputException("Evaluator returned null rubric scores");
        }

        Map<RubricDimension, Double> normalized = new EnumMap<>(RubricDimension.class);
        for (RubricDimension dimension : RubricDimension.values()) {
            if (!scores.containsKey(dimension)) {
                throw new InvalidAiOutputException("Evaluator missing score for " + dimension);
            }
            double value = clamp(scores.get(dimension), 0, 100);
            normalized.put(dimension, round2(value));
        }
        return normalized;
    }

    private Map<String, Double> toResponseScores(Map<RubricDimension, Double> rubricScores) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("requirement_understanding", rubricScores.get(RubricDimension.REQUIREMENT_UNDERSTANDING));
        result.put("logical_clarity", rubricScores.get(RubricDimension.LOGICAL_CLARITY));
        result.put("technical_feasibility", rubricScores.get(RubricDimension.TECHNICAL_FEASIBILITY));
        result.put("edge_case_coverage", rubricScores.get(RubricDimension.EDGE_CASE_COVERAGE));
        result.put("communication_structure", rubricScores.get(RubricDimension.COMMUNICATION_STRUCTURE));
        return result;
    }

    private double averageScore(Long userId) {
        Double average = evaluationRepository.findAverageFinalScoreByUserId(userId);
        return average == null ? 0 : average;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
