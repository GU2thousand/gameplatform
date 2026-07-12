package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.TestAuth;
import com.gamingplatform.ai.impl.HeuristicEvaluationAiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AsyncFailureIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean HeuristicEvaluationAiClient evaluator;

    @Test
    void shouldPersistFailedStatusWhenAsyncEvaluationThrows() throws Exception {
        when(evaluator.evaluate(any(), any())).thenThrow(new IllegalStateException("simulated evaluator outage"));
        var user = TestAuth.register(mockMvc, mapper);
        long challengeId = mapper.readTree(mockMvc.perform(post("/api/challenge/generate")
                        .session(user.session()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("id").asLong();
        long submissionId = mapper.readTree(mockMvc.perform(post("/api/submissions")
                        .session(user.session()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("challengeId", challengeId, "answer",
                                "This answer is long enough to pass validation but evaluation will fail."))))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString())
                .path("submissionId").asLong();

        for (int i = 0; i < 100; i++) {
            JsonNode response = mapper.readTree(mockMvc.perform(get("/api/submissions/{id}", submissionId)
                            .session(user.session())).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString());
            if (response.path("status").asText().equals("FAILED")) {
                assertThat(response.path("errorMessage").asText()).contains("Submission evaluation failed");
                return;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("submission did not transition to FAILED");
    }
}
