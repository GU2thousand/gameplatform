package com.gamingplatform.ai;

import com.gamingplatform.entity.Difficulty;

public interface ChallengeAiClient {

    GeneratedChallenge generate(Difficulty difficulty);
}
