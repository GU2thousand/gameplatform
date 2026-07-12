package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.legacy-claim-secret=test-migration-secret")
@AutoConfigureMockMvc
class LegacyClaimIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test
    void shouldSecureLegacyAccountAndRestoreItsChallengeHistory() throws Exception {
        String username = "legacy_" + UUID.randomUUID();
        jdbc.update("insert into users(username,password_hash,xp,created_at) values (?,null,?,?)",
                username, 10, Instant.now());
        Long userId = jdbc.queryForObject("select id from users where username=?", Long.class, username);
        jdbc.update("insert into challenges(title,difficulty,context,expected_output_format,created_at) " +
                        "values (?,?,?,?,?)", "Migrated challenge", "INTERMEDIATE", "Legacy context", "Markdown",
                Instant.now());
        Long challengeId = jdbc.queryForObject("select id from challenges where title='Migrated challenge' " +
                "order by id desc limit 1", Long.class);
        jdbc.update("insert into submissions(user_id,challenge_id,answer,answer_hash,idempotency_key,status,submitted_at) " +
                        "values (?,?,?,?,?,?,?)", userId, challengeId, "Legacy answer", "0".repeat(64),
                "legacy-test-" + UUID.randomUUID(), "COMPLETED", Instant.now());

        var result = mockMvc.perform(post("/api/auth/claim-legacy").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", username,
                                "newPassword", TestAuth.PASSWORD,
                                "claimSecret", "test-migration-secret"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username)).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/api/challenges").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + challengeId + ")]").exists());
        mockMvc.perform(get("/api/challenges/{id}/attempts", challengeId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}
