package com.gamingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.repository.UserProfileRepository;
import com.gamingplatform.security.SessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:session-security-tests;MODE=MYSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.ai.provider=local",
        "app.session.cookie-secure=false",
        "app.rate-limit.expensive-per-minute=2"
})
@AutoConfigureMockMvc
class SessionSecurityIntegrationTest {
    private static final String VALID_ANSWER =
            "Use an authenticated API with a durable database, input validation, idempotency keys, and retry limits.";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserProfileRepository users;

    @Test
    void sessionUsesProtectedHttpsCookieStoresOnlyItsHashAndRestoresExistingIdentity() throws Exception {
        long countBefore = users.count();
        MockHttpServletResponse created = mockMvc.perform(json(post("/api/session"), "{}").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.sessionTokenHash").doesNotExist())
                .andReturn().getResponse();

        String setCookie = created.getHeader("Set-Cookie");
        assertThat(setCookie).contains("HttpOnly", "SameSite=Strict", "Secure", "Path=/", "Max-Age=");
        BrowserSession session = sessionFrom(created);
        assertThat(session.cookie().getValue()).matches("[A-Za-z0-9_-]{43}");
        var persisted = users.findById(session.id()).orElseThrow();
        assertThat(persisted.getSessionTokenHash()).isEqualTo(SessionService.hash(session.cookie().getValue()))
                .hasSize(64).isNotEqualTo(session.cookie().getValue());
        assertThat(persisted.getSessionExpiresAt()).isAfter(Instant.now());
        assertThat(users.count()).isEqualTo(countBefore + 1);

        mockMvc.perform(json(post("/api/session"), "{}").cookie(session.cookie()).secure(true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session.id()))
                .andExpect(header().doesNotExist("Set-Cookie"));
        assertThat(users.count()).isEqualTo(countBefore + 1);
    }

    @Test
    void unauthenticatedAndForgedCookiesCannotAccessPrivateApis() throws Exception {
        for (String path : new String[]{"/api/challenge/current", "/api/challenge/1", "/api/user/me/progress", "/api/user/me/history"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mockMvc.perform(json(post("/api/challenge/generate"), "{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user/me/progress").cookie(new Cookie(SessionService.COOKIE, "a".repeat(43))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/debug/ai-mode"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void suffixAndTrailingSlashDoNotBypassAuthentication() throws Exception {
        for (String path : new String[]{"/api/challenge/current/", "/api/challenge/current.json"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void ambiguousPathsCannotBypassAuthenticationOrReachSharedLegacyWorkspace() throws Exception {
        for (String path : new String[]{
                "/api;v=1/challenge/current",
                "/api/challenge/current;v=1",
                "/%61pi/challenge/current",
                "/a%70i/challenge/current",
                "/api%2fchallenge/current",
                "/api/%63hallenge/current",
                "/api//challenge/current",
                "/api/./challenge/current",
                "/api/user/../challenge/current"
        }) {
            mockMvc.perform(get(URI.create(path)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.challenge").doesNotExist())
                    .andExpect(jsonPath("$.draftAnswer").doesNotExist());
        }
        for (String path : new String[]{
                "/api;v=1/challenge/generate",
                "/api/challenge/generate;v=1",
                "/%61pi/challenge/generate",
                "/api%2Fchallenge/generate",
                "/api/challenge/generate%3Bv=1"
        }) {
            mockMvc.perform(json(post(URI.create(path)), "{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.id").doesNotExist());
        }
    }

    @Test
    void crossSiteRequestsCannotCreateSessionsOrMutateAnExistingSession() throws Exception {
        long countBefore = users.count();
        mockMvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Set-Cookie"));
        for (String origin : new String[]{"https://evil.example", "http://localhost:81", "null", "/relative"}) {
            mockMvc.perform(json(post("/api/session"), "{}").header("Origin", origin))
                    .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Set-Cookie"));
        }
        mockMvc.perform(json(post("/api/session"), "{}").header("Sec-Fetch-Site", "cross-site"))
                .andExpect(status().isForbidden());
        assertThat(users.count()).isEqualTo(countBefore);

        BrowserSession session = sessionFrom(mockMvc.perform(json(post("/api/session"), "{}")
                        .header("Origin", "http://localhost").header("Sec-Fetch-Site", "same-origin"))
                .andExpect(status().isOk()).andReturn().getResponse());
        mockMvc.perform(json(post("/api/challenge/generate"), "{}").cookie(session.cookie())
                        .header("Origin", "https://evil.example"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/challenge/generate").cookie(session.cookie())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/challenge/generate").cookie(session.cookie())
                        .header("X-Requested-With", "career-platform").contentType(MediaType.TEXT_PLAIN).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void oneBrowserCannotDiscoverReadChangeOrSubmitAnotherBrowsersWorkspace() throws Exception {
        BrowserSession owner = startSession();
        long challengeId = generate(owner);
        String privateAnswer = "Private draft belonging only to the owner.";
        mockMvc.perform(json(put("/api/challenge/{id}/draft", challengeId), Map.of("answer", privateAnswer))
                        .cookie(owner.cookie()))
                .andExpect(status().isNoContent());
        BrowserSession stranger = startSession();

        mockMvc.perform(get("/api/challenge/current").cookie(stranger.cookie()).param("userId", Long.toString(owner.id())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.challenge").isEmpty())
                .andExpect(jsonPath("$.draftAnswer").value(""));
        mockMvc.perform(get("/api/user/me/history").cookie(stranger.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/challenge/{id}", challengeId).cookie(stranger.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(put("/api/challenge/{id}/draft", challengeId), Map.of("answer", "Overwritten"))
                        .cookie(stranger.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(post("/api/submission"), Map.of("challengeId", challengeId, "answer", VALID_ANSWER))
                        .cookie(stranger.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/user/{id}/progress", owner.id()).cookie(stranger.cookie()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/challenge/{id}", challengeId).cookie(owner.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draftAnswer").value(privateAnswer));
        mockMvc.perform(get("/api/user/me/progress").cookie(owner.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.xp").value(0));
    }

    @Test
    void aSubmittedUserIdCannotImpersonateAnotherIdentity() throws Exception {
        BrowserSession owner = startSession();
        BrowserSession stranger = startSession();
        long challengeId = generate(owner);
        mockMvc.perform(json(post("/api/submission"), Map.of("userId", stranger.id(), "challengeId", challengeId,
                                "answer", VALID_ANSWER)).cookie(owner.cookie()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/user/me/progress").cookie(stranger.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.xp").value(0))
                .andExpect(jsonPath("$.completedChallenges").value(0));
    }

    @Test
    void malformedJsonAndUnknownDifficultyReturnBadRequest() throws Exception {
        BrowserSession session = startSession();
        mockMvc.perform(json(post("/api/challenge/generate"), "{\"difficulty\":").cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(post("/api/challenge/generate"), "{\"difficulty\":\"IMPOSSIBLE\"}").cookie(session.cookie()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generationRejectsOversizedTextAndListsBeforeCreatingAChallenge() throws Exception {
        BrowserSession session = startSession();
        mockMvc.perform(json(post("/api/challenge/generate"), Map.of("roleTrack", "x".repeat(121)))
                        .cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(post("/api/challenge/generate"), Map.of("customRequirements",
                                java.util.Collections.nCopies(13, "A requirement"))).cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/challenge/current").cookie(session.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.challenge").isEmpty());
    }

    @Test
    void draftsRejectMissingOrOversizedAnswersWithoutChangingSavedContent() throws Exception {
        BrowserSession session = startSession();
        long challengeId = generate(session);
        mockMvc.perform(json(put("/api/challenge/{id}/draft", challengeId), "{}").cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(put("/api/challenge/{id}/draft", challengeId), Map.of("answer", "x".repeat(10001)))
                        .cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/challenge/{id}", challengeId).cookie(session.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draftAnswer").value(""));
    }

    @Test
    void submissionsRejectInvalidAndOversizedAnswersBeforeLookupOrScoring() throws Exception {
        BrowserSession session = startSession();
        mockMvc.perform(json(post("/api/submission"), Map.of("challengeId", 1, "answer", "short"))
                        .cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(post("/api/submission"), Map.of("challengeId", 1, "answer", "x".repeat(10001)))
                        .cookie(session.cookie()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/user/me/progress").cookie(session.cookie()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.xp").value(0));
    }

    @Test
    void requestSizeLimitRejectsHugePayloadBeforeCreatingSession() throws Exception {
        long countBefore = users.count();
        mockMvc.perform(json(post("/api/session"), Map.of("payload", "x".repeat(32769))))
                .andExpect(status().isPayloadTooLarge()).andExpect(header().doesNotExist("Set-Cookie"));
        assertThat(users.count()).isEqualTo(countBefore);
    }

    @Test
    void expensiveRequestsAreRateLimitedPerBrowserIdentity() throws Exception {
        BrowserSession limited = startSession();
        generate(limited);
        generate(limited);
        mockMvc.perform(json(post("/api/challenge/generate"), "{}").cookie(limited.cookie()))
                .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "60"));
        mockMvc.perform(get("/api/user/me/progress").cookie(limited.cookie()))
                .andExpect(status().isOk());
        generate(startSession());
    }

    @Test
    void expiredSessionCannotRecoverPrivateWorkspace() throws Exception {
        BrowserSession session = startSession();
        var user = users.findById(session.id()).orElseThrow();
        user.setSessionExpiresAt(Instant.now().minusSeconds(1));
        users.saveAndFlush(user);
        mockMvc.perform(get("/api/user/me/progress").cookie(session.cookie()))
                .andExpect(status().isUnauthorized());
    }

    private BrowserSession startSession() throws Exception {
        return sessionFrom(mockMvc.perform(json(post("/api/session"), "{}"))
                .andExpect(status().isOk()).andReturn().getResponse());
    }

    private BrowserSession sessionFrom(MockHttpServletResponse response) throws Exception {
        String cookiePair = response.getHeader("Set-Cookie").split(";", 2)[0];
        return new BrowserSession(mapper.readTree(response.getContentAsString()).get("id").asLong(),
                new Cookie(SessionService.COOKIE, cookiePair.substring(cookiePair.indexOf('=') + 1)));
    }

    private long generate(BrowserSession session) throws Exception {
        String response = mockMvc.perform(json(post("/api/challenge/generate"), "{}").cookie(session.cookie()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.header("X-Requested-With", "career-platform").contentType(MediaType.APPLICATION_JSON)
                .content(body instanceof String value ? value : mapper.writeValueAsString(body));
    }

    private record BrowserSession(long id, Cookie cookie) {}
}
