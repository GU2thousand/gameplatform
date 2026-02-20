package com.gamingplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
}
