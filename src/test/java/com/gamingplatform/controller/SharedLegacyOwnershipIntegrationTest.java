package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SharedLegacyOwnershipIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test
    void shouldAttributeRetryToAuthenticatedLegacyUserAndPreserveHistoryWhenOwnerDeletes() throws Exception {
        var owner = TestAuth.register(mockMvc, mapper);
        var second = TestAuth.register(mockMvc, mapper);
        long challengeId = mapper.readTree(mockMvc.perform(post("/api/challenge/generate")
                        .session(owner.session()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("id").asLong();

        jdbc.update("insert into submissions(user_id,challenge_id,answer,answer_hash,idempotency_key,status,submitted_at,completed_at) " +
                        "values (?,?,?,?,?,?,?,?)", second.id(), challengeId, "Legacy shared answer",
                "0".repeat(64), "legacy-shared-" + UUID.randomUUID(), "COMPLETED", Instant.now(), Instant.now());

        long retryId = mapper.readTree(mockMvc.perform(post("/api/submissions")
                        .session(second.session()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("challengeId", challengeId,
                                "answer", "A new detailed retry owned by the authenticated second user with tradeoffs."))))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString())
                .path("submissionId").asLong();
        assertThat(jdbc.queryForObject("select user_id from submissions where id=?", Long.class, retryId))
                .isEqualTo(second.id());

        mockMvc.perform(delete("/api/account").session(owner.session()).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/challenges/{id}", challengeId).session(second.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(challengeId));
        assertThat(jdbc.queryForObject("select created_by_id from challenges where id=?", Long.class, challengeId))
                .isEqualTo(second.id());
    }
}
