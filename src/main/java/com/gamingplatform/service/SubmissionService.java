package com.gamingplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.dto.AttemptComparisonResponse;
import com.gamingplatform.dto.EvaluationResponse;
import com.gamingplatform.dto.SubmissionRequest;
import com.gamingplatform.dto.SubmissionStatusResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.SalaryTier;
import com.gamingplatform.entity.Submission;
import com.gamingplatform.entity.SubmissionStatus;
import com.gamingplatform.exception.ConflictException;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubmissionService {
    private final ChallengeService challenges;
    private final SubmissionRepository submissions;
    private final EvaluationRepository evaluations;
    private final RecommendationService recommendations;
    private final ApplicationEventPublisher events;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactions;
    private final AiQuotaService aiQuotaService;
    private final UserService userService;

    public SubmissionService(ChallengeService challenges, SubmissionRepository submissions,
                             EvaluationRepository evaluations, RecommendationService recommendations,
                             ApplicationEventPublisher events, ObjectMapper objectMapper,
                             PlatformTransactionManager transactionManager, AiQuotaService aiQuotaService,
                             UserService userService) {
        this.challenges = challenges;
        this.submissions = submissions;
        this.evaluations = evaluations;
        this.recommendations = recommendations;
        this.events = events;
        this.objectMapper = objectMapper;
        this.transactions = new TransactionTemplate(transactionManager);
        this.aiQuotaService = aiQuotaService;
        this.userService = userService;
    }

    public SubmissionStatusResponse enqueue(Long userId, SubmissionRequest request, String headerKey) {
        if (request == null) throw new IllegalArgumentException("Submission request is required");
        String key = resolveKey(request.getIdempotencyKey(), headerKey);
        Challenge challenge = challenges.getOwnedById(request.getChallengeId(), userId);
        String answerHash = hash(challenge.getId() + "\n" + request.getAnswer());

        Long submissionId;
        try {
            submissionId = transactions.execute(status -> {
                Submission existing = submissions.findByUser_IdAndIdempotencyKey(userId, key).orElse(null);
                if (existing != null) {
                    validateSamePayload(existing, challenge.getId(), answerHash);
                    return existing.getId();
                }
                aiQuotaService.consume(userId, "evaluation", key);
                Submission submission = new Submission();
                submission.setUser(userService.getById(userId));
                submission.setChallenge(challenge);
                submission.setAnswer(request.getAnswer());
                submission.setAnswerHash(answerHash);
                submission.setIdempotencyKey(key);
                submission.setStatus(SubmissionStatus.PENDING);
                submission = submissions.saveAndFlush(submission);
                events.publishEvent(new SubmissionQueuedEvent(submission.getId()));
                return submission.getId();
            });
        } catch (DataIntegrityViolationException race) {
            Submission existing = transactions.execute(status -> submissions
                    .findByUser_IdAndIdempotencyKey(userId, key)
                    .orElseThrow(() -> race));
            validateSamePayload(existing, challenge.getId(), answerHash);
            submissionId = existing.getId();
        }
        return get(userId, submissionId);
    }

    public SubmissionStatusResponse get(Long userId, Long submissionId) {
        return transactions.execute(status -> {
            Submission submission = owned(userId, submissionId);
            Evaluation evaluation = evaluations.findBySubmission_Id(submissionId).orElse(null);
            return toResponse(submission, evaluation);
        });
    }

    public List<SubmissionStatusResponse> history(Long userId) {
        return transactions.execute(status -> submissions.findByUser_IdOrderBySubmittedAtDesc(userId).stream()
                .map(submission -> toResponse(submission,
                        evaluations.findBySubmission_Id(submission.getId()).orElse(null)))
                .toList());
    }

    public List<SubmissionStatusResponse> attempts(Long userId, Long challengeId) {
        challenges.getOwnedById(challengeId, userId);
        return transactions.execute(status -> submissions
                .findByChallenge_IdAndUser_IdOrderBySubmittedAtDesc(challengeId, userId).stream()
                .map(submission -> toResponse(submission,
                        evaluations.findBySubmission_Id(submission.getId()).orElse(null)))
                .toList());
    }

    public AttemptComparisonResponse compare(Long userId, Long firstId, Long secondId) {
        SubmissionStatusResponse first = get(userId, firstId);
        SubmissionStatusResponse second = get(userId, secondId);
        if (first.evaluation() == null || second.evaluation() == null) {
            throw new IllegalArgumentException("Both attempts must be completed");
        }
        if (!first.challengeId().equals(second.challengeId())) {
            throw new IllegalArgumentException("Attempts must belong to the same challenge");
        }
        Map<String, Double> deltas = new LinkedHashMap<>();
        List<String> improved = new ArrayList<>();
        List<String> declined = new ArrayList<>();
        first.evaluation().rubricScores().forEach((dimension, score) -> {
            double delta = round2(second.evaluation().rubricScores().getOrDefault(dimension, 0.0) - score);
            deltas.put(dimension, delta);
            if (delta > 0) improved.add(dimension);
            if (delta < 0) declined.add(dimension);
        });
        return new AttemptComparisonResponse(first, second,
                round2(second.evaluation().finalScore() - first.evaluation().finalScore()),
                deltas, improved, declined);
    }

    private Submission owned(Long userId, Long id) {
        return submissions.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Submission not found: " + id));
    }

    private SubmissionStatusResponse toResponse(Submission submission, Evaluation evaluation) {
        EvaluationResponse evaluationResponse = evaluation == null ? null : evaluationResponse(evaluation);
        return new SubmissionStatusResponse(submission.getId(), submission.getStatus(),
                submission.getChallenge().getId(), submission.getChallenge().getTitle(),
                submission.getIdempotencyKey(), submission.getSubmittedAt(), submission.getCompletedAt(),
                submission.getErrorMessage(), evaluationResponse);
    }

    private EvaluationResponse evaluationResponse(Evaluation evaluation) {
        Map<String, Double> scores = scores(evaluation);
        Map<String, Double> weights = normalizeKeys(readMap(evaluation.getRubricWeightsJson()));
        SalaryTier tier = SalaryTier.fromScore(evaluation.getFinalScore());
        Map<RubricDimension, Double> recommendationScores = new java.util.EnumMap<>(RubricDimension.class);
        recommendationScores.put(RubricDimension.REQUIREMENT_UNDERSTANDING, evaluation.getRequirementUnderstanding());
        recommendationScores.put(RubricDimension.LOGICAL_CLARITY, evaluation.getLogicalClarity());
        recommendationScores.put(RubricDimension.TECHNICAL_FEASIBILITY, evaluation.getTechnicalFeasibility());
        recommendationScores.put(RubricDimension.EDGE_CASE_COVERAGE, evaluation.getEdgeCaseCoverage());
        recommendationScores.put(RubricDimension.COMMUNICATION_STRUCTURE, evaluation.getCommunicationStructure());
        return new EvaluationResponse(evaluation.getId(), round2(evaluation.getFinalScore()), tier,
                tier.getTitle(), scores, weights, evaluation.getFeedback(), readList(evaluation.getStrengthsJson()),
                readList(evaluation.getImprovementsJson()), evaluation.getExampleOutline(),
                recommendations.buildImprovementTrack(recommendationScores), evaluation.getProvider());
    }

    private Map<String, Double> scores(Evaluation evaluation) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("requirement_understanding", evaluation.getRequirementUnderstanding());
        result.put("logical_clarity", evaluation.getLogicalClarity());
        result.put("technical_feasibility", evaluation.getTechnicalFeasibility());
        result.put("edge_case_coverage", evaluation.getEdgeCaseCoverage());
        result.put("communication_structure", evaluation.getCommunicationStructure());
        return result;
    }

    private Map<String, Double> normalizeKeys(Map<String, Double> source) {
        Map<String, Double> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key.toLowerCase(), value));
        return result;
    }

    private Map<String, Double> readMap(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception ex) { return Map.of(); }
    }

    private List<String> readList(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception ex) { return List.of(); }
    }

    private String resolveKey(String bodyKey, String headerKey) {
        String body = normalizeKey(bodyKey);
        String header = normalizeKey(headerKey);
        if (body != null && header != null && !body.equals(header)) {
            throw new ConflictException("Idempotency keys in header and body differ");
        }
        return body != null ? body : header != null ? header : UUID.randomUUID().toString();
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > 100) throw new IllegalArgumentException("Idempotency key is too long");
        return normalized;
    }

    private void validateSamePayload(Submission existing, Long challengeId, String answerHash) {
        boolean legacySentinel = "0".repeat(64).equals(existing.getAnswerHash());
        boolean sameAnswer = legacySentinel ? hash(challengeId + "\n" + existing.getAnswer()).equals(answerHash)
                : existing.getAnswerHash().equals(answerHash);
        if (!existing.getChallenge().getId().equals(challengeId) || !sameAnswer) {
            throw new ConflictException("Idempotency key was already used for a different submission");
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private double round2(double value) { return Math.round(value * 100.0) / 100.0; }
}
