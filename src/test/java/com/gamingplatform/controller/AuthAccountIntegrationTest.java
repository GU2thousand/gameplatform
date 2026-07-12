package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAccountIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test
    void shouldExposeCsrfAndRequireItForRegistration() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").isNotEmpty());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", "no_csrf_user",
                                "password", TestAuth.PASSWORD))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldExportWithoutCredentialsAndDeleteAccountWithSession() throws Exception {
        var user = TestAuth.register(mockMvc, mapper);
        jdbc.update("insert into challenges(created_by_id,title,difficulty,context,expected_output_format,generation_provider,created_at) " +
                        "values (?,?,?,?,?,?,?)", user.id(), "Delete me", "INTERMEDIATE", "context", "Markdown",
                "local", Instant.now());
        Long challengeId = jdbc.queryForObject("select id from challenges where created_by_id=? and title='Delete me'",
                Long.class, user.id());
        jdbc.update("insert into submissions(user_id,challenge_id,answer,answer_hash,idempotency_key,status,submitted_at,completed_at) " +
                        "values (?,?,?,?,?,?,?,?)", user.id(), challengeId, "answer", "0".repeat(64),
                UUID.randomUUID().toString(), "COMPLETED", Instant.now(), Instant.now());
        Long submissionId = jdbc.queryForObject("select id from submissions where user_id=? and challenge_id=?",
                Long.class, user.id(), challengeId);
        jdbc.update("insert into evaluations(submission_id,requirement_understanding,logical_clarity,technical_feasibility," +
                        "edge_case_coverage,communication_structure,final_score,feedback,provider,strengths_json," +
                        "improvements_json,example_outline,rubric_weights_json,created_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                submissionId, 70, 70, 70, 70, 70, 70, "feedback", "legacy", "[\"strength\"]",
                "[\"improvement\"]", "outline", "{}", Instant.now());
        mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", user.username(),
                                "password", "Definitely-Wrong-123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        mockMvc.perform(get("/api/account/export").session(user.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(2))
                .andExpect(jsonPath("$.profile.username").value(user.username()))
                .andExpect(jsonPath("$.submissions[0].evaluation.strengths[0]").value("strength"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(not(containsString("passwordHash"))));

        mockMvc.perform(delete("/api/account").session(user.session()).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("select count(*) from users where id=?", Long.class, user.id())).isZero();
        assertThat(jdbc.queryForObject("select count(*) from challenges where id=?", Long.class, challengeId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from submissions where id=?", Long.class, submissionId)).isZero();
        mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", user.username(),
                                "password", TestAuth.PASSWORD))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
