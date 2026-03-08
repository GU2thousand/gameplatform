package com.gamingplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.ai.provider=langchain4j",
        "app.ai.langchain4j.openai.api-key=dummy-key",
        "app.ai.langchain4j.openai.model-name=gpt-4o-mini"
})
@AutoConfigureMockMvc
class DebugControllerLangChain4jTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReportLangChain4jMode() throws Exception {
        mockMvc.perform(get("/api/debug/ai-mode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("langchain4j"))
                .andExpect(jsonPath("$.challengeClient").value("LangChain4jChallengeAiClient"))
                .andExpect(jsonPath("$.evaluationClient").value("LangChain4jEvaluationAiClient"))
                .andExpect(jsonPath("$.openAiApiKeyConfigured").value(true));
    }
}
