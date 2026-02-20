package com.gamingplatform.ai;

import com.gamingplatform.entity.Difficulty;

import java.util.List;

public record GeneratedChallenge(
        String title,
        Difficulty difficulty,
        String context,
        List<String> requirements,
        List<String> constraints,
        List<String> acceptanceCriteria,
        String expectedOutputFormat
) {
}
