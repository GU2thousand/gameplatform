package com.gamingplatform.service;

import com.gamingplatform.GamingPlatformApplication;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.dto.SubmissionRequest;
import com.gamingplatform.security.SessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class WorkspacePersistenceTest {
    @TempDir Path directory;

    @Test void sessionChallengeDraftEvaluationAndXpSurviveApplicationRestart() {
        Cookie credential;
        long userId;
        long challengeId;
        long submissionId;
        int xp;
        String answer = "Propose an API with a database, retry queue and explicit failure monitoring, idempotency and a tested rollout plan.";
        try (var first = start()) {
            var request = new MockHttpServletRequest();
            var response = new MockHttpServletResponse();
            userId = first.getBean(SessionService.class).start(request, response).id();
            credential = new Cookie(SessionService.COOKIE, response.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1]);
            challengeId = first.getBean(ChallengeService.class).generate(
                    new ChallengeGenerationInput(null, null, null, null, null, null, null, null), userId).id();
            first.getBean(WorkspaceService.class).saveDraft(challengeId, userId, answer);
            var submission = new SubmissionRequest();
            submission.setChallengeId(challengeId);
            submission.setAnswer(answer);
            submissionId = first.getBean(SubmissionService.class).submit(submission, userId).submissionId();
            xp = first.getBean(ProgressService.class).getProgress(userId).xp();
        }
        try (var restarted = start()) {
            var request = new MockHttpServletRequest();
            request.setCookies(credential);
            var restoredUser = restarted.getBean(SessionService.class).resolve(request).orElseThrow();
            assertThat(restoredUser.getId()).isEqualTo(userId);
            var state = restarted.getBean(WorkspaceService.class).current(userId);
            assertThat(state.challenge().id()).isEqualTo(challengeId);
            assertThat(state.draftAnswer()).isEqualTo(answer);
            assertThat(state.result().submissionId()).isEqualTo(submissionId);
            var repeated = new SubmissionRequest(); repeated.setChallengeId(challengeId); repeated.setAnswer(answer);
            assertThat(restarted.getBean(SubmissionService.class).submit(repeated, userId).submissionId()).isEqualTo(submissionId);
            assertThat(restarted.getBean(ProgressService.class).getProgress(userId).xp()).isEqualTo(xp);
            assertThat(restarted.getBean(WorkspaceService.class).history(userId)).hasSize(1);
        }
        assertThat(directory.resolve("workspace.mv.db")).exists();
    }

    private ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(GamingPlatformApplication.class).web(WebApplicationType.NONE).run(
                "--spring.datasource.url=jdbc:h2:file:" + directory.resolve("workspace") + ";MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE",
                "--spring.jpa.hibernate.ddl-auto=update", "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }
}
