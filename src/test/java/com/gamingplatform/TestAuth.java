package com.gamingplatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestAuth {
    public static final String PASSWORD = "Correct-Horse-123!";

    private TestAuth() {}

    public static AuthenticatedUser register(MockMvc mockMvc, ObjectMapper mapper) throws Exception {
        String username = "qa_" + UUID.randomUUID();
        var result = mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isCreated()).andReturn();
        long id = mapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
        return new AuthenticatedUser(id, username, (MockHttpSession) result.getRequest().getSession(false));
    }

    public record AuthenticatedUser(long id, String username, MockHttpSession session) {}
}
