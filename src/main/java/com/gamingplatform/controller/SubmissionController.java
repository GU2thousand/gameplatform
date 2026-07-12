package com.gamingplatform.controller;

import com.gamingplatform.dto.AttemptComparisonResponse;
import com.gamingplatform.dto.SubmissionRequest;
import com.gamingplatform.dto.SubmissionStatusResponse;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.SubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/submissions", "/api/submission"})
@Validated
public class SubmissionController {
    private final SubmissionService submissions;
    private final CurrentUserService currentUser;

    public SubmissionController(SubmissionService submissions, CurrentUserService currentUser) {
        this.submissions = submissions;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmissionStatusResponse submit(@Valid @RequestBody SubmissionRequest request,
                                           @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        return submissions.enqueue(currentUser.requireUserId(), request, key);
    }

    @GetMapping
    public List<SubmissionStatusResponse> history() {
        return submissions.history(currentUser.requireUserId());
    }

    @GetMapping("/{id}")
    public SubmissionStatusResponse get(@PathVariable @Positive Long id) {
        return submissions.get(currentUser.requireUserId(), id);
    }

    @GetMapping("/compare")
    public AttemptComparisonResponse compare(@RequestParam @Positive Long firstId,
                                             @RequestParam @Positive Long secondId) {
        return submissions.compare(currentUser.requireUserId(), firstId, secondId);
    }
}
