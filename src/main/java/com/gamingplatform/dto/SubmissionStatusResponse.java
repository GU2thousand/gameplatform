package com.gamingplatform.dto;

import com.gamingplatform.entity.SubmissionStatus;

import java.time.Instant;

public record SubmissionStatusResponse(
        Long submissionId,
        SubmissionStatus status,
        Long challengeId,
        String challengeTitle,
        String idempotencyKey,
        Instant submittedAt,
        Instant completedAt,
        String errorMessage,
        EvaluationResponse evaluation
) {}
