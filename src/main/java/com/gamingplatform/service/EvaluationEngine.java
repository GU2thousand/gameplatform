package com.gamingplatform.service;

import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.ai.impl.HeuristicEvaluationAiClient;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.exception.AiServiceException;
import com.gamingplatform.exception.InvalidAiOutputException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EvaluationEngine {
    private static final Logger log = LoggerFactory.getLogger(EvaluationEngine.class);
    private static final int MAX_FEEDBACK_LENGTH = 5000;

    private final EvaluationAiClient primary;
    private final HeuristicEvaluationAiClient fallback;
    private final RecommendationService recommendations;
    private final MeterRegistry meters;

    public EvaluationEngine(EvaluationAiClient primary, HeuristicEvaluationAiClient fallback,
                            RecommendationService recommendations, MeterRegistry meters) {
        this.primary = primary;
        this.fallback = fallback;
        this.recommendations = recommendations;
        this.meters = meters;
    }

    public EvaluatedAnswer evaluate(Challenge challenge, String answer) {
        Timer.Sample sample = Timer.start(meters);
        try {
            ValidatedEvaluation value;
            String provider;
            try {
                value = validate(primary.evaluate(challenge, answer));
                provider = primary == fallback ? "local" : "langchain4j";
            } catch (RuntimeException primaryFailure) {
                if (primary == fallback) throw primaryFailure;
                log.warn("Primary evaluation failed; using local fallback: {}",
                        primaryFailure.getClass().getSimpleName());
                value = validate(fallback.evaluate(challenge, answer));
                provider = "fallback";
                meters.counter("gamingplatform.ai.fallback", "operation", "evaluation").increment();
            }

            Map<RubricDimension, Double> weights = weightsFor(challenge.getChallengeType());
            Map<RubricDimension, Double> scores = value.scores();
            double finalScore = round2(weights.entrySet().stream()
                    .mapToDouble(entry -> entry.getValue() * scores.get(entry.getKey())).sum());
            List<Map.Entry<RubricDimension, Double>> ordered = scores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toList();
            List<String> strengths = ordered.stream().limit(2)
                    .map(entry -> recommendations.dimensionLabel(entry.getKey()) + "：证据较完整（"
                            + round2(entry.getValue()) + "/100）。").toList();
            List<String> improvements = ordered.stream().skip(Math.max(0, ordered.size() - 2L))
                    .map(entry -> recommendations.dimensionLabel(entry.getKey()) + "：补充具体依据、边界与取舍（"
                            + round2(entry.getValue()) + "/100）。").toList();
            return new EvaluatedAnswer(scores, weights, finalScore, value.feedback(), provider,
                    strengths, improvements, exampleOutline(challenge.getChallengeType()));
        } catch (RuntimeException failure) {
            meters.counter("gamingplatform.ai.failures", "operation", "evaluation").increment();
            if (failure instanceof AiServiceException) throw failure;
            throw new AiServiceException("Submission evaluation failed", failure);
        } finally {
            sample.stop(meters.timer("gamingplatform.ai.duration", "operation", "evaluation"));
        }
    }

    public Map<RubricDimension, Double> weightsFor(String challengeType) {
        String type = challengeType == null ? "" : challengeType.toLowerCase(Locale.ROOT);
        if (type.contains("prd") || type.contains("product")) {
            return weights(.35, .25, .10, .10, .20);
        }
        if (type.contains("system") || type.contains("architecture")) {
            return weights(.15, .15, .35, .25, .10);
        }
        if (type.contains("api")) {
            return weights(.20, .15, .35, .20, .10);
        }
        return weights(.25, .20, .25, .15, .15);
    }

    private Map<RubricDimension, Double> weights(double requirement, double logic, double technical,
                                                  double edge, double communication) {
        Map<RubricDimension, Double> result = new EnumMap<>(RubricDimension.class);
        result.put(RubricDimension.REQUIREMENT_UNDERSTANDING, requirement);
        result.put(RubricDimension.LOGICAL_CLARITY, logic);
        result.put(RubricDimension.TECHNICAL_FEASIBILITY, technical);
        result.put(RubricDimension.EDGE_CASE_COVERAGE, edge);
        result.put(RubricDimension.COMMUNICATION_STRUCTURE, communication);
        return Map.copyOf(result);
    }

    private ValidatedEvaluation validate(EvaluationResult result) {
        if (result == null || result.rubricScores() == null) {
            throw new InvalidAiOutputException("Evaluator returned no scores");
        }
        Map<RubricDimension, Double> scores = new EnumMap<>(RubricDimension.class);
        for (RubricDimension dimension : RubricDimension.values()) {
            Double raw = result.rubricScores().get(dimension);
            if (raw == null || !Double.isFinite(raw)) {
                throw new InvalidAiOutputException("Evaluator returned invalid score for " + dimension);
            }
            scores.put(dimension, round2(Math.max(0, Math.min(100, raw))));
        }
        String feedback = result.feedback();
        if (feedback == null || feedback.isBlank() || feedback.trim().length() > MAX_FEEDBACK_LENGTH) {
            throw new InvalidAiOutputException("Evaluator returned invalid feedback");
        }
        return new ValidatedEvaluation(Map.copyOf(scores), feedback.trim());
    }

    private String exampleOutline(String challengeType) {
        String type = challengeType == null ? "" : challengeType.toLowerCase(Locale.ROOT);
        if (type.contains("prd") || type.contains("product")) {
            return "1. Problem & target user\n2. Goals and non-goals\n3. User stories\n4. Requirements\n5. Metrics\n6. Risks and rollout";
        }
        if (type.contains("api")) {
            return "1. Use cases\n2. Resources and endpoints\n3. Request/response examples\n4. Errors and idempotency\n5. Security\n6. Versioning and observability";
        }
        return "1. Requirements and scale\n2. High-level architecture\n3. Data model and APIs\n4. Critical flows\n5. Reliability and edge cases\n6. Tradeoffs and observability";
    }

    private double round2(double value) { return Math.round(value * 100.0) / 100.0; }

    private record ValidatedEvaluation(Map<RubricDimension, Double> scores, String feedback) {}

    public record EvaluatedAnswer(Map<RubricDimension, Double> scores,
                                  Map<RubricDimension, Double> weights,
                                  double finalScore,
                                  String feedback,
                                  String provider,
                                  List<String> strengths,
                                  List<String> improvements,
                                  String exampleOutline) {}
}
