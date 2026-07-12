package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class ApiValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private TestAuth.AuthenticatedUser user;

    @BeforeEach
    void authenticate() throws Exception { user = TestAuth.register(mockMvc, objectMapper); }

    @Test
    void shouldRejectMalformedJsonAndInvalidEnumAsBadRequests() throws Exception {
        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"IMPOSSIBLE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/challenge/generate"));
    }

    @Test
    void shouldEnforceChallengeCustomizationBounds() throws Exception {
        String oversizedText = "x".repeat(1201);
        String oversizedItem = "x".repeat(401);

        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "businessContext", oversizedText,
                                "customRequirements", Collections.singletonList(oversizedItem)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("businessContext")))
                .andExpect(jsonPath("$.message", containsString("customRequirements")));

        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customConstraints", Collections.nCopies(11, "constraint")
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("customConstraints")));

        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customAcceptanceCriteria\":[\"   \"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("customAcceptanceCriteria")));
    }

    @Test
    void shouldSafelyHandleMaximumCombinedTitleInputs() throws Exception {
        mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "challengeType", "t".repeat(120),
                                "focusGoal", "f".repeat(160)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", hasLength(255)));
    }

    @Test
    void shouldRejectInvalidIdentifiersBeforeRepositoryAccess() throws Exception {
        String validAnswer = "A sufficiently detailed answer with more than thirty characters.";

        mockMvc.perform(post("/api/submission")
                        .session(user.session()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", 0,
                                "challengeId", -1,
                                "answer", validAnswer
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("userId")))
                .andExpect(jsonPath("$.message", containsString("challengeId")));

        mockMvc.perform(get("/api/user/{id}/progress", 0).session(user.session()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/user/{id}/progress", "not-a-number").session(user.session()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id: invalid value"));
    }

    @Test
    void shouldNormalizeUsernameAndReturnConflictForDuplicate() throws Exception {
        String username = "qa_" + UUID.randomUUID();

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "  " + username + "  ",
                                "password", TestAuth.PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username,
                                "password", TestAuth.PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }
}
