package com.gamingplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReportCurrentAiMode() throws Exception {
        mockMvc.perform(post("/api/debug/ai-mode").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.challengeClient").value("TemplateChallengeAiClient"))
                .andExpect(jsonPath("$.evaluationClient").value("HeuristicEvaluationAiClient"))
                .andExpect(jsonPath("$.openAiModel").value("gpt-4o-mini"));
    }

    @Test
    void shouldReportCurrentAiModeViaGet() throws Exception {
        mockMvc.perform(get("/api/debug/ai-mode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.challengeClient").value("TemplateChallengeAiClient"))
                .andExpect(jsonPath("$.evaluationClient").value("HeuristicEvaluationAiClient"));
    }
}
