package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.ai.EvaluationResult;
import com.gamingplatform.entity.RubricDimension;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"app.ai.max-concurrent-requests=1", "spring.datasource.hikari.maximum-pool-size=2"})
@AutoConfigureMockMvc
class SlowEvaluationAdmissionTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean EvaluationAiClient evaluator;

    @Test void slowEvaluationRejectsQueuedMutationsWhileProgressReadsStillWork() throws Exception {
        Cookie session = start();
        long id = json.readTree(mvc.perform(body(post("/api/challenge/generate").cookie(session), "{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asLong();
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        when(evaluator.evaluate(any(), any())).thenAnswer(call -> {
            entered.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Test evaluator timed out");
            Map<RubricDimension, Double> scores = new EnumMap<>(RubricDimension.class);
            for (var dimension : RubricDimension.values()) scores.put(dimension, 75.0);
            return new EvaluationResult(scores, "Clear architecture with useful reliability controls.");
        });
        String answer = json.writeValueAsString(Map.of("challengeId", id, "answer", "Design an API and database with retries, idempotency, monitoring and a clear rollout plan."));
        var pool = Executors.newSingleThreadExecutor();
        try {
            var first = pool.submit(() -> mvc.perform(body(post("/api/submission").cookie(session), answer))
                    .andExpect(status().isOk()).andReturn());
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            mvc.perform(body(post("/api/submission").cookie(session), answer)).andExpect(status().isTooManyRequests());
            mvc.perform(body(put("/api/challenge/{id}/draft", id).cookie(session), "{\"answer\":\"queued draft\"}"))
                    .andExpect(status().isTooManyRequests());
            mvc.perform(get("/api/user/me/progress").cookie(session)).andExpect(status().isOk());
            release.countDown();
            first.get(10, TimeUnit.SECONDS);
            mvc.perform(body(post("/api/submission").cookie(session), answer)).andExpect(status().isOk());
        } finally { release.countDown(); pool.shutdownNow(); }
    }
    private Cookie start() throws Exception {
        String cookie = mvc.perform(body(post("/api/session"), "{}")).andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Set-Cookie");
        return new Cookie("gp_session", cookie.split(";", 2)[0].split("=", 2)[1]);
    }
    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, String value) {
        return request.header("X-Requested-With", "career-platform").contentType(MediaType.APPLICATION_JSON).content(value);
    }
}
