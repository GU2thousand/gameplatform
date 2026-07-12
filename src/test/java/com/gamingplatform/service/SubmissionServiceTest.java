package com.gamingplatform.service;

import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.ai.impl.HeuristicEvaluationAiClient;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.exception.AiServiceException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {
    @Mock EvaluationAiClient primary;
    @Mock HeuristicEvaluationAiClient fallback;
    private EvaluationEngine engine;
    private Challenge challenge;

    @BeforeEach
    void setUp() {
        engine = new EvaluationEngine(primary, fallback, new RecommendationService(), new SimpleMeterRegistry());
        challenge = new Challenge();
        challenge.setChallengeType("System Design");
    }

    @Test
    void shouldFallbackBeforePersistenceWhenPrimaryOutputIsInvalid() {
        when(primary.evaluate(challenge, "answer")).thenReturn(new EvaluationResult(scores(Double.NaN), "bad"));
        when(fallback.evaluate(challenge, "answer")).thenReturn(new EvaluationResult(scores(70), "fallback"));
        EvaluationEngine.EvaluatedAnswer result = engine.evaluate(challenge, "answer");
        assertThat(result.provider()).isEqualTo("fallback");
        assertThat(result.finalScore()).isEqualTo(70);
    }

    @Test
    void shouldFailWhenBothEvaluatorsReturnInvalidFeedback() {
        when(primary.evaluate(challenge, "answer")).thenReturn(new EvaluationResult(scores(70), " "));
        when(fallback.evaluate(challenge, "answer")).thenReturn(new EvaluationResult(scores(70), null));
        assertThatThrownBy(() -> engine.evaluate(challenge, "answer"))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    void shouldUseTypeSpecificRubricWeights() {
        Map<RubricDimension, Double> weights = engine.weightsFor("System Design");
        assertThat(weights.get(RubricDimension.TECHNICAL_FEASIBILITY)).isEqualTo(.35);
        assertThat(weights.values().stream().mapToDouble(Double::doubleValue).sum()).isEqualTo(1.0);
    }

    private Map<RubricDimension, Double> scores(double value) {
        Map<RubricDimension, Double> scores = new EnumMap<>(RubricDimension.class);
        for (RubricDimension dimension : RubricDimension.values()) scores.put(dimension, value);
        return scores;
    }
}
