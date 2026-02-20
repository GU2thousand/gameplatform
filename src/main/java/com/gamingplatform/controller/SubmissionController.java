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

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public SubmissionResponse submit(@Valid @RequestBody SubmissionRequest request) {
        return submissionService.submit(request);
    }
}
