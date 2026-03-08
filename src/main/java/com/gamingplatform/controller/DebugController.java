package com.gamingplatform.controller;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.EvaluationAiClient;
import com.gamingplatform.config.LangChain4jOpenAiProperties;
import com.gamingplatform.dto.AiModeDebugResponse;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final Environment environment;
    private final ChallengeAiClient challengeAiClient;
    private final EvaluationAiClient evaluationAiClient;
    private final LangChain4jOpenAiProperties openAiProperties;

    public DebugController(
            Environment environment,
            ChallengeAiClient challengeAiClient,
            EvaluationAiClient evaluationAiClient,
            LangChain4jOpenAiProperties openAiProperties
    ) {
        this.environment = environment;
        this.challengeAiClient = challengeAiClient;
        this.evaluationAiClient = evaluationAiClient;
        this.openAiProperties = openAiProperties;
    }

    @GetMapping("/ai-mode")
    public AiModeDebugResponse aiModeGet() {
        return buildResponse();
    }

    @PostMapping("/ai-mode")
    public AiModeDebugResponse aiModePost() {
        return buildResponse();
    }

    private AiModeDebugResponse buildResponse() {
        return new AiModeDebugResponse(
                environment.getProperty("app.ai.provider", "local"),
                challengeAiClient.getClass().getSimpleName(),
                evaluationAiClient.getClass().getSimpleName(),
                Arrays.asList(environment.getActiveProfiles()),
                openAiProperties.getModelName(),
                StringUtils.hasText(openAiProperties.getApiKey()),
                StringUtils.hasText(openAiProperties.getBaseUrl())
        );
    }
}
