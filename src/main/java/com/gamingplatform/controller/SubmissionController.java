package com.gamingplatform.controller;

import com.gamingplatform.dto.SubmissionRequest;
import com.gamingplatform.dto.SubmissionResponse;
import com.gamingplatform.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final com.gamingplatform.service.AiWorkLimiter limiter;

    public SubmissionController(SubmissionService submissionService, com.gamingplatform.service.AiWorkLimiter limiter) {
        this.submissionService = submissionService;
        this.limiter = limiter;
    }

    @PostMapping
    public SubmissionResponse submit(@Valid @RequestBody SubmissionRequest request, jakarta.servlet.http.HttpServletRequest http) {
        return limiter.execute(() -> submissionService.submit(request, com.gamingplatform.security.SessionService.currentUser(http)));
    }
}
