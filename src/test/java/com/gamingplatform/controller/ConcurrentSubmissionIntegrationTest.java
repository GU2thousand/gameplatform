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
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentSubmissionIntegrationTest {
    private static final int CONCURRENT_RETRIES = 20;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void shouldReturnOneJobAndAwardXpOnceForConcurrentIdempotentRetries() throws Exception {
        var user = TestAuth.register(mockMvc, objectMapper);
        long challengeId = objectMapper.readTree(mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("id").asLong();
        String key = UUID.randomUUID().toString();
        String body = objectMapper.writeValueAsString(Map.of("challengeId", challengeId, "answer",
                "A complete answer with API, queue, database, retry, idempotency, edge cases, metrics and tradeoffs."));

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_RETRIES);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<MvcResult>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < CONCURRENT_RETRIES; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return mockMvc.perform(post("/api/submissions").session(user.session()).with(csrf().asHeader())
                                    .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
                            .andExpect(status().isAccepted()).andReturn();
                }));
            }
            start.countDown();
            Long id = null;
            for (Future<MvcResult> future : futures) {
                long actual = objectMapper.readTree(future.get().getResponse().getContentAsString())
                        .path("submissionId").asLong();
                if (id == null) id = actual;
                assertThat(actual).isEqualTo(id);
            }
            awaitTerminal(user, id);
            JsonNode progress = objectMapper.readTree(mockMvc.perform(get("/api/user/me/progress")
                            .session(user.session())).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString());
            assertThat(progress.path("totalAttempts").asLong()).isEqualTo(1);
            assertThat(progress.path("completedChallenges").asLong()).isEqualTo(1);
            assertThat(progress.path("xp").asInt()).isGreaterThan(0);
        } finally {
            executor.shutdownNow();
        }
    }

    private void awaitTerminal(TestAuth.AuthenticatedUser user, long id) throws Exception {
        for (int i = 0; i < 100; i++) {
            JsonNode response = objectMapper.readTree(mockMvc.perform(get("/api/submissions/{id}", id)
                            .session(user.session())).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString());
            if (response.path("status").asText().matches("COMPLETED|FAILED")) {
                assertThat(response.path("status").asText()).isEqualTo("COMPLETED");
                return;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("submission did not complete");
    }
}
