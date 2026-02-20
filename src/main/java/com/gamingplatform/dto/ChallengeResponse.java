package com.gamingplatform.dto;

import com.gamingplatform.entity.Difficulty;

import java.time.Instant;
import java.util.List;

public record ChallengeResponse(
        Long id,
        String title,
        Difficulty difficulty,
        String context,
        List<String> requirements,
        List<String> constraints,
        List<String> acceptanceCriteria,
        String expectedOutputFormat,
        Instant createdAt
) {
}
