package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSubmitAndFetchProgress() throws Exception {
        String username = "user_" + UUID.randomUUID();

        String userResponse = mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = readLong(userResponse, "id");

        String challengeResponse = mockMvc.perform(post("/api/challenge/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"INTERMEDIATE\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long challengeId = readLong(challengeResponse, "id");

        String answer = """
                # Architecture
                First, I propose an API gateway plus queue and worker model.
                Then we persist notification state in a database and add retry with timeout.
                ## API Design
                - POST /notifications with idempotency key
                - GET /notifications/{id}
                ## Reliability
                We handle duplicate requests, failure fallback, dead letter replay, and rate limit controls.
                We monitor latency, throughput, and SLO with dashboards.
                ## Tradeoff
                We accept at-least-once delivery and mitigate duplicate delivery at consumer side.
                """;

        String submissionBody = """
                {
                  "userId": %d,
                  "challengeId": %d,
                  "answer": %s
                }
                """.formatted(userId, challengeId, objectMapper.writeValueAsString(answer));

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").exists())
                .andExpect(jsonPath("$.finalScore").isNumber())
                .andExpect(jsonPath("$.salaryTier").isString())
                .andExpect(jsonPath("$.rubricScores.technical_feasibility").isNumber());

        mockMvc.perform(get("/api/user/{id}/progress", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedChallenges").value(1))
                .andExpect(jsonPath("$.averageScore").isNumber())
                .andExpect(jsonPath("$.weakestDimension").isString());
    }

    private long readLong(String json, String field) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.get(field).asLong();
    }
}
