package com.gamingplatform.dto;

import com.gamingplatform.entity.Difficulty;

public class ChallengeGenerateRequest {

    private Difficulty difficulty = Difficulty.INTERMEDIATE;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }
}
