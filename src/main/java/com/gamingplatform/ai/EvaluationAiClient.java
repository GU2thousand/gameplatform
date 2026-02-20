package com.gamingplatform.ai;

import com.gamingplatform.entity.Challenge;

public interface EvaluationAiClient {

    EvaluationResult evaluate(Challenge challenge, String answer);
}
