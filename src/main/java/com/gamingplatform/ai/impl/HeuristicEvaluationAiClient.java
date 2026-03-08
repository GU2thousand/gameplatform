package com.gamingplatform.ai.impl;

import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.RubricDimension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "local", matchIfMissing = true)
public class HeuristicEvaluationAiClient implements EvaluationAiClient {

    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^#{1,6}\\s+");
    private static final Pattern BULLET_PATTERN = Pattern.compile("(?m)^[-*]\\s+");

    @Override
    public EvaluationResult evaluate(Challenge challenge, String answer) {
        String normalized = answer.toLowerCase(Locale.ROOT);

        Map<RubricDimension, Double> scores = new EnumMap<>(RubricDimension.class);
        scores.put(RubricDimension.REQUIREMENT_UNDERSTANDING, requirementUnderstanding(challenge, normalized));
        scores.put(RubricDimension.LOGICAL_CLARITY, logicalClarity(normalized, answer));
        scores.put(RubricDimension.TECHNICAL_FEASIBILITY, technicalFeasibility(normalized));
        scores.put(RubricDimension.EDGE_CASE_COVERAGE, edgeCaseCoverage(normalized));
        scores.put(RubricDimension.COMMUNICATION_STRUCTURE, communicationStructure(normalized, answer));

        String feedback = buildFeedback(scores);
        return new EvaluationResult(scores, feedback);
    }

    private double requirementUnderstanding(Challenge challenge, String normalizedAnswer) {
        int hits = 0;
        for (String req : challenge.getRequirements()) {
            for (String token : tokenize(req)) {
                if (token.length() >= 5 && normalizedAnswer.contains(token)) {
                    hits++;
                    break;
                }
            }
        }
        return clamp(45 + hits * 16, 35, 95);
    }

    private double logicalClarity(String normalizedAnswer, String rawAnswer) {
        int headingCount = countMatches(HEADING_PATTERN, rawAnswer);
        int bulletCount = countMatches(BULLET_PATTERN, rawAnswer);

        int connectors = countKeywords(normalizedAnswer,
                List.of("first", "then", "therefore", "because", "finally", "tradeoff", "step"));

        double score = 40 + headingCount * 10 + bulletCount * 2 + connectors * 3;
        return clamp(score, 30, 95);
    }

    private double technicalFeasibility(String normalizedAnswer) {
        int keywordHits = countKeywords(normalizedAnswer,
                List.of("api", "database", "cache", "queue", "retry", "timeout", "latency",
                        "throughput", "scalability", "idempot", "monitor", "slo", "rate limit"));
        double score = 40 + keywordHits * 4.5;
        return clamp(score, 35, 97);
    }

    private double edgeCaseCoverage(String normalizedAnswer) {
        int edgeHits = countKeywords(normalizedAnswer,
                List.of("edge", "failure", "duplicate", "fallback", "degraded", "partition",
                        "throttle", "dead letter", "replay", "out-of-order", "retry"));

        double score = 32 + edgeHits * 7.5;
        return clamp(score, 25, 96);
    }

    private double communicationStructure(String normalizedAnswer, String rawAnswer) {
        int words = Math.max(1, normalizedAnswer.split("\\s+").length);
        int headingCount = countMatches(HEADING_PATTERN, rawAnswer);
        int bulletCount = countMatches(BULLET_PATTERN, rawAnswer);

        double lengthBonus;
        if (words < 120) {
            lengthBonus = -10;
        } else if (words <= 800) {
            lengthBonus = 12;
        } else {
            lengthBonus = 4;
        }

        double score = 45 + lengthBonus + headingCount * 6 + bulletCount * 1.5;
        return clamp(score, 30, 95);
    }

    private String buildFeedback(Map<RubricDimension, Double> scores) {
        List<Map.Entry<RubricDimension, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort(Comparator.comparingDouble(Map.Entry::getValue));

        RubricDimension weakest = sorted.get(0).getKey();
        RubricDimension secondWeakest = sorted.get(1).getKey();

        return "Strong baseline solution. Improve " + label(weakest)
                + " and " + label(secondWeakest)
                + " by adding explicit tradeoffs, failure handling, and clearer acceptance mapping.";
    }

    private int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countKeywords(String text, List<String> keywords) {
        int hits = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                hits++;
            }
        }
        return hits;
    }

    private List<String> tokenize(String sentence) {
        String[] parts = sentence.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+");

        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String label(RubricDimension dimension) {
        return switch (dimension) {
            case REQUIREMENT_UNDERSTANDING -> "requirement understanding";
            case LOGICAL_CLARITY -> "logical clarity";
            case TECHNICAL_FEASIBILITY -> "technical feasibility";
            case EDGE_CASE_COVERAGE -> "edge case coverage";
            case COMMUNICATION_STRUCTURE -> "communication structure";
        };
    }
}
