package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private static final String ANSWER = """
            # Architecture
            First, propose an API gateway plus queue and worker model.
            Then persist notification state in a database and add retry with timeout.
            ## API Design
            - POST /notifications with idempotency key
            - GET /notifications/{id}
            ## Reliability
            Handle duplicates, failure fallback, dead letter replay, and rate limit controls.
            Monitor latency, throughput, and SLO with dashboards.
            ## Tradeoff
            Accept at-least-once delivery and mitigate duplicates at the consumer.
            """;

    private Cookie session() throws Exception {
        var response = mvc.perform(post("/api/session").header("X-Requested-With", "career-platform")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse();
        return new Cookie("gp_session", response.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1]);
    }
    private long challenge(Cookie cookie) throws Exception {
        return json.readTree(mvc.perform(post("/api/challenge/generate").cookie(cookie)
                        .header("X-Requested-With", "career-platform").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    private JsonNode submit(Cookie cookie, long challenge, String answer) throws Exception {
        return json.readTree(mvc.perform(post("/api/submission").cookie(cookie)
                        .header("X-Requested-With", "career-platform").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("challengeId", challenge, "answer", answer))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode progress(Cookie cookie) throws Exception {
        return json.readTree(mvc.perform(get("/api/user/me/progress").cookie(cookie))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    @Test void draftResultHistoryAndProgressRestoreFromServer() throws Exception {
        Cookie cookie = session(); long id = challenge(cookie);
        mvc.perform(put("/api/challenge/{id}/draft", id).cookie(cookie).header("X-Requested-With", "career-platform")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("answer", ANSWER))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/challenge/current").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge.id").value(id)).andExpect(jsonPath("$.draftAnswer").value(ANSWER));
        JsonNode first = submit(cookie, id, ANSWER);
        JsonNode duplicate = submit(cookie, id, "  " + ANSWER + "  ");
        assertThat(duplicate).isEqualTo(first);
        assertThat(progress(cookie).get("xp").asInt()).isEqualTo(first.get("xpAwarded").asInt());
        assertThat(progress(cookie).get("completedChallenges").asInt()).isEqualTo(1);
        mvc.perform(get("/api/challenge/current").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.draftAnswer").value(ANSWER.strip()))
                .andExpect(jsonPath("$.result.submissionId").value(first.get("submissionId").asLong()));
        challenge(cookie);
        mvc.perform(get("/api/user/me/history").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[1].challenge.id").value(id));
    }

    @Test void reusedOlderEvaluationIsTheResultRestoredWithItsAnswer() throws Exception {
        Cookie cookie = session(); long id = challenge(cookie);
        JsonNode first = submit(cookie, id, ANSWER);
        submit(cookie, id, "A different basic answer with a database and API for the stated requirements.");
        JsonNode repeated = submit(cookie, id, ANSWER);
        assertThat(repeated).isEqualTo(first);
        mvc.perform(get("/api/challenge/current").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.draftAnswer").value(ANSWER.strip()))
                .andExpect(jsonPath("$.result.submissionId").value(first.get("submissionId").asLong()));
    }

    @Test void concurrentIdenticalSubmissionsAwardXpOnlyOnce() throws Exception {
        Cookie cookie = session(); long id = challenge(cookie);
        var pool = Executors.newFixedThreadPool(3);
        try {
            List<Callable<JsonNode>> calls = new ArrayList<>();
            for (int i = 0; i < 3; i++) calls.add(() -> submit(cookie, id, ANSWER));
            var results = pool.invokeAll(calls);
            JsonNode first = results.get(0).get();
            for (var result : results) assertThat(result.get()).isEqualTo(first);
            assertThat(progress(cookie).get("xp").asInt()).isEqualTo(first.get("xpAwarded").asInt());
            assertThat(progress(cookie).get("completedChallenges").asInt()).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    @Test void improvedOrWorseAnswersOnlyAwardBestScoreDelta() throws Exception {
        Cookie cookie = session(); long id = challenge(cookie);
        JsonNode basic = submit(cookie, id, "A simple proposal should describe the requested business requirements.");
        JsonNode improved = submit(cookie, id, ANSWER);
        JsonNode worse = submit(cookie, id, "This is a different but still basic answer about the business requirements.");
        int best = Math.max(Math.max((int) Math.round(basic.get("finalScore").asDouble()),
                (int) Math.round(improved.get("finalScore").asDouble())), (int) Math.round(worse.get("finalScore").asDouble()));
        assertThat(progress(cookie).get("xp").asInt()).isEqualTo(best);
        assertThat(progress(cookie).get("completedChallenges").asInt()).isEqualTo(1);
        assertThat(basic.get("xpAwarded").asInt() + improved.get("xpAwarded").asInt() + worse.get("xpAwarded").asInt()).isEqualTo(best);
    }
}
