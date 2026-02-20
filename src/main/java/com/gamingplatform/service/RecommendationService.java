package com.gamingplatform.service;

import com.gamingplatform.entity.RubricDimension;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final Map<RubricDimension, String> trackMapping = new EnumMap<>(RubricDimension.class);

    public RecommendationService() {
        trackMapping.put(RubricDimension.REQUIREMENT_UNDERSTANDING,
                "Practice PRD deconstruction and acceptance criteria tracing.");
        trackMapping.put(RubricDimension.LOGICAL_CLARITY,
                "Practice structured answer outlines and step-by-step reasoning drills.");
        trackMapping.put(RubricDimension.TECHNICAL_FEASIBILITY,
                "Practice system design tradeoff analysis and feasibility checks.");
        trackMapping.put(RubricDimension.EDGE_CASE_COVERAGE,
                "Practice reliability review checklists: retries, idempotency, and failure modes.");
        trackMapping.put(RubricDimension.COMMUNICATION_STRUCTURE,
                "Practice concise technical writing with explicit sections and bullet points.");
    }

    public String buildImprovementTrack(Map<RubricDimension, Double> rubricScores) {
        return weakestDimension(rubricScores)
                .map(trackMapping::get)
                .orElse("Maintain performance with mixed mock interview challenges.");
    }

    public List<String> recommendations(RubricDimension weakestDimension) {
        if (weakestDimension == null) {
            return List.of("Submit at least one challenge to unlock recommendations.");
        }

        return List.of(
                trackMapping.get(weakestDimension),
                "Run one timed mock interview this week and compare rubric deltas.",
                "Focus on one weak dimension per attempt to improve score stability."
        );
    }

    public String dimensionLabel(RubricDimension dimension) {
        if (dimension == null) {
            return "N/A";
        }
        return switch (dimension) {
            case REQUIREMENT_UNDERSTANDING -> "Requirement Understanding";
            case LOGICAL_CLARITY -> "Logical Clarity";
            case TECHNICAL_FEASIBILITY -> "Technical Feasibility";
            case EDGE_CASE_COVERAGE -> "Edge Case Coverage";
            case COMMUNICATION_STRUCTURE -> "Communication Structure";
        };
    }

    public java.util.Optional<RubricDimension> weakestDimension(Map<RubricDimension, Double> rubricScores) {
        if (rubricScores == null || rubricScores.isEmpty()) {
            return java.util.Optional.empty();
        }
        return rubricScores.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }
}
