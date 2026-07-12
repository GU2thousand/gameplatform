package com.gamingplatform.controller;

import com.gamingplatform.dto.AttemptComparisonResponse;
import com.gamingplatform.dto.SubmissionStatusResponse;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.SubmissionService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class AttemptController {
    private final SubmissionService submissions;
    private final CurrentUserService currentUser;

    public AttemptController(SubmissionService submissions, CurrentUserService currentUser) {
        this.submissions = submissions;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/challenges/{challengeId}/attempts")
    public List<SubmissionStatusResponse> attempts(@PathVariable @Positive Long challengeId) {
        return submissions.attempts(currentUser.requireUserId(), challengeId);
    }

    @GetMapping("/api/attempts/compare")
    public AttemptComparisonResponse compare(@RequestParam @Positive Long firstId,
                                             @RequestParam @Positive Long secondId) {
        return submissions.compare(currentUser.requireUserId(), firstId, secondId);
    }
}
