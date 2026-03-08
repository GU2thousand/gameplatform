package com.gamingplatform.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.exception.InvalidAiOutputException;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "langchain4j")
public class LangChain4jEvaluationAiClient implements EvaluationAiClient {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LangChain4jEvaluationAiClient(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public EvaluationResult evaluate(Challenge challenge, String answer) {
        String raw = chatModel.chat(buildPrompt(challenge, answer));

        try {
            JsonNode root = objectMapper.readTree(LangChain4jJsonSupport.extractJsonObject(raw));
            JsonNode rubricNode = root.path("rubricScores").isObject() ? root.path("rubricScores") : root;

            Map<RubricDimension, Double> scores = new EnumMap<>(RubricDimension.class);
            for (RubricDimension dimension : RubricDimension.values()) {
                JsonNode scoreNode = rubricNode.path(dimension.name());
                if (!scoreNode.isNumber() && !scoreNode.isTextual()) {
                    throw new InvalidAiOutputException("Missing rubric score for " + dimension.name());
                }
                scores.put(dimension, scoreNode.asDouble());
            }

            String feedback = root.path("feedback").asText("");
            if (feedback.isBlank()) {
                feedback = "Add clearer tradeoffs, explicit edge cases, and stronger requirement traceability.";
            }

            return new EvaluationResult(scores, feedback.trim());
        } catch (Exception ex) {
            throw new InvalidAiOutputException("LangChain4j evaluation returned invalid JSON: " + ex.getMessage());
        }
    }

    private String buildPrompt(Challenge challenge, String answer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are a senior engineering interviewer evaluating a candidate response.
                Score the answer from 0 to 100 on each rubric dimension and provide concise structured feedback.
                
                Return ONLY valid JSON (no markdown, no code fences) in this exact schema:
                {
                  "rubricScores": {
                    "REQUIREMENT_UNDERSTANDING": 0,
                    "LOGICAL_CLARITY": 0,
                    "TECHNICAL_FEASIBILITY": 0,
                    "EDGE_CASE_COVERAGE": 0,
                    "COMMUNICATION_STRUCTURE": 0
                  },
                  "feedback": "string"
                }
                
                Scoring rules:
                - Be strict but fair.
                - Use integers or decimals between 0 and 100.
                - Consider requirements coverage, feasibility, and edge cases.
                - Feedback should mention 2-3 concrete improvements.
                
                Challenge:
                """);

        prompt.append("\nTitle: ").append(challenge.getTitle());
        prompt.append("\nDifficulty: ").append(challenge.getDifficulty());
        prompt.append("\nContext: ").append(challenge.getContext());
        prompt.append("\nRequirements:\n").append(formatList(challenge.getRequirements()));
        prompt.append("\nConstraints:\n").append(formatList(challenge.getConstraints()));
        prompt.append("\nAcceptance Criteria:\n").append(formatList(challenge.getAcceptanceCriteria()));
        prompt.append("\nExpected Output Format: ").append(challenge.getExpectedOutputFormat());
        prompt.append("\n\nCandidate Answer:\n");
        prompt.append(answer);

        return prompt.toString();
    }

    private String formatList(List<String> items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            builder.append("- ").append(items.get(i));
            if (i < items.size() - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
