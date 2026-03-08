package com.gamingplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateChallenge() throws Exception {
        mockMvc.perform(post("/api/challenge/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"INTERMEDIATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.requirements").isArray())
                .andExpect(jsonPath("$.constraints").isArray());
    }

    @Test
    void shouldGenerateChallengeFromCustomInputs() throws Exception {
        mockMvc.perform(post("/api/challenge/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "difficulty":"INTERMEDIATE",
                                  "roleTrack":"PM",
                                  "challengeType":"API Design",
                                  "focusGoal":"latency reduction and observability",
                                  "businessContext":"A fintech app is seeing slow balance lookups during market open.",
                                  "customRequirements":["Include rollout metrics and monitoring checkpoints."],
                                  "customConstraints":["Must stay under 150ms p95."],
                                  "customAcceptanceCriteria":["Explain how success will be measured after rollout."]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", containsString("API Design")))
                .andExpect(jsonPath("$.context", containsString("A fintech app is seeing slow balance lookups during market open.")))
                .andExpect(jsonPath("$.requirements[*]", hasItem("Include rollout metrics and monitoring checkpoints.")))
                .andExpect(jsonPath("$.constraints[*]", hasItem("Must stay under 150ms p95.")))
                .andExpect(jsonPath("$.acceptanceCriteria[*]", hasItem("Explain how success will be measured after rollout.")));
    }
}
