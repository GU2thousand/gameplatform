package com.gamingplatform.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Difficulty;
import com.gamingplatform.exception.InvalidAiOutputException;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Primary
@ConditionalOnBean(ChatModel.class)
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "langchain4j")
public class LangChain4jChallengeAiClient implements ChallengeAiClient {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LangChain4jChallengeAiClient(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeneratedChallenge generate(Difficulty difficulty) {
        Difficulty requested = difficulty == null ? Difficulty.INTERMEDIATE : difficulty;
        String raw = chatModel.chat(buildPrompt(requested));

        try {
            JsonNode root = objectMapper.readTree(LangChain4jJsonSupport.extractJsonObject(raw));

            return new GeneratedChallenge(
                    readText(root, "title"),
                    parseDifficulty(root.path("difficulty").asText(null), requested),
                    readText(root, "context"),
                    readStringList(root, "requirements"),
                    readStringList(root, "constraints"),
                    readStringList(root, "acceptanceCriteria"),
                    readText(root, "expectedOutputFormat")
            );
        } catch (Exception ex) {
            throw new InvalidAiOutputException("LangChain4j challenge generation returned invalid JSON: " + ex.getMessage());
        }
    }

    private String buildPrompt(Difficulty difficulty) {
        return """
                You are a senior Product Manager creating a realistic challenge for an AI gamified PM/SDE training platform.
                
                Generate ONE challenge for difficulty: %s
                
                Return ONLY valid JSON (no markdown, no code fences) using this exact schema:
                {
                  "title": "string",
                  "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
                  "context": "string",
                  "requirements": ["string", "string", "string"],
                  "constraints": ["string", "string", "string"],
                  "acceptanceCriteria": ["string", "string", "string"],
                  "expectedOutputFormat": "Markdown"
                }
                
                Rules:
                - Keep it realistic for product + engineering collaboration.
                - Include concrete scale, latency, or operational constraints.
                - difficulty must exactly equal requested difficulty.
                - Each list must contain at least 3 items.
                - expectedOutputFormat should be Markdown.
                """.formatted(difficulty.name());
    }

    private String readText(JsonNode root, String field) {
        String value = root.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new InvalidAiOutputException("Missing field: " + field);
        }
        return value.trim();
    }

    private List<String> readStringList(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (!node.isArray() || node.isEmpty()) {
            throw new InvalidAiOutputException("Invalid list field: " + field);
        }

        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String text = item.asText(null);
            if (text == null || text.isBlank()) {
                throw new InvalidAiOutputException("Blank item in field: " + field);
            }
            values.add(text.trim());
        }
        return values;
    }

    private Difficulty parseDifficulty(String rawDifficulty, Difficulty fallback) {
        if (rawDifficulty == null || rawDifficulty.isBlank()) {
            return fallback;
        }
        try {
            return Difficulty.valueOf(rawDifficulty.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
