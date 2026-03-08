package com.gamingplatform.config;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.EvaluationAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class AiModeStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AiModeStartupLogger.class);

    private final Environment environment;
    private final ChallengeAiClient challengeAiClient;
    private final EvaluationAiClient evaluationAiClient;
    private final LangChain4jOpenAiProperties openAiProperties;

    public AiModeStartupLogger(
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

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "AI mode: provider={}, challengeClient={}, evaluationClient={}, activeProfiles={}, openAiModel={}, openAiApiKeyConfigured={}, openAiBaseUrlConfigured={}",
                environment.getProperty("app.ai.provider", "local"),
                challengeAiClient.getClass().getSimpleName(),
                evaluationAiClient.getClass().getSimpleName(),
                Arrays.toString(environment.getActiveProfiles()),
                openAiProperties.getModelName(),
                StringUtils.hasText(openAiProperties.getApiKey()),
                StringUtils.hasText(openAiProperties.getBaseUrl())
        );
    }
}
