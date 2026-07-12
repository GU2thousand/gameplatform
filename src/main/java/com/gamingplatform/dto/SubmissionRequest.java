package com.gamingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SubmissionRequest {

    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long challengeId;

    @NotBlank
    @Size(min = 30, max = 10000)
    private String answer;

    @Size(max = 100)
    private String idempotencyKey;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer == null ? null : answer.trim();
    }

    public String getIdempotencyKey() { return idempotencyKey; }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey == null ? null : idempotencyKey.trim();
    }
}
