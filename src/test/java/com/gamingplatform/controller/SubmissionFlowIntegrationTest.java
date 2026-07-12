package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionFlowIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void shouldReturnStableZeroProgressForNewUser() throws Exception {
        var user = TestAuth.register(mockMvc, objectMapper);
        mockMvc.perform(get("/api/user/me/progress").session(user.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xp").value(0))
                .andExpect(jsonPath("$.completedChallenges").value(0))
                .andExpect(jsonPath("$.totalAttempts").value(0))
                .andExpect(jsonPath("$.last7Days.length()").value(7));
    }

    @Test
    void shouldAsynchronouslyEvaluateAndReturnTransparentFeedback() throws Exception {
        var user = TestAuth.register(mockMvc, objectMapper);
        long challengeId = createChallenge(user);
        String answer = "# Architecture\nUse an API gateway, queue, durable database, retry, timeout, " +
                "idempotency, monitoring, SLOs, failure fallback, edge cases, tradeoffs, and rollout metrics.";
        String key = UUID.randomUUID().toString();
        JsonNode accepted = submit(user, challengeId, answer, key);
        long submissionId = accepted.path("submissionId").asLong();
        JsonNode completed = awaitCompletion(user, submissionId);
        org.assertj.core.api.Assertions.assertThat(completed.path("status").asText()).isEqualTo("COMPLETED");
        org.assertj.core.api.Assertions.assertThat(completed.path("evaluation").path("provider").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(completed.path("evaluation").path("strengths").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(completed.path("evaluation").path("rubricWeights").size()).isEqualTo(5);

        JsonNode replay = submit(user, challengeId, answer, key);
        org.assertj.core.api.Assertions.assertThat(replay.path("submissionId").asLong()).isEqualTo(submissionId);

        mockMvc.perform(post("/api/submissions").session(user.session()).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("challengeId", challengeId,
                                "answer", answer + " Different payload."))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/user/me/progress").session(user.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedChallenges").value(1))
                .andExpect(jsonPath("$.totalAttempts").value(1));
    }

    @Test
    void shouldPreventCrossAccountResourceAccess() throws Exception {
        var owner = TestAuth.register(mockMvc, objectMapper);
        var attacker = TestAuth.register(mockMvc, objectMapper);
        long challengeId = createChallenge(owner);
        mockMvc.perform(get("/api/challenges/{id}", challengeId).session(attacker.session()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/submissions").session(attacker.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("challengeId", challengeId,
                                "answer", "A detailed answer that is definitely longer than thirty characters."))))
                .andExpect(status().isNotFound());
    }

    private long createChallenge(TestAuth.AuthenticatedUser user) throws Exception {
        String response = mockMvc.perform(post("/api/challenge/generate").session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"INTERMEDIATE\",\"challengeType\":\"System Design\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("id").asLong();
    }

    private JsonNode submit(TestAuth.AuthenticatedUser user, long challengeId, String answer, String key)
            throws Exception {
        String response = mockMvc.perform(post("/api/submissions").session(user.session()).with(csrf())
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("challengeId", challengeId, "answer", answer))))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode awaitCompletion(TestAuth.AuthenticatedUser user, long submissionId) throws Exception {
        for (int i = 0; i < 100; i++) {
            String response = mockMvc.perform(get("/api/submissions/{id}", submissionId).session(user.session()))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JsonNode json = objectMapper.readTree(response);
            if (json.path("status").asText().matches("COMPLETED|FAILED")) return json;
            Thread.sleep(25);
        }
        throw new AssertionError("submission did not complete");
    }
}
