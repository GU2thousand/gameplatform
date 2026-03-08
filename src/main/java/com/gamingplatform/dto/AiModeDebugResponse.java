package com.gamingplatform.dto;

import java.util.List;

public record AiModeDebugResponse(
        String provider,
        String challengeClient,
        String evaluationClient,
        List<String> activeProfiles,
        String openAiModel,
        boolean openAiApiKeyConfigured,
        boolean openAiBaseUrlConfigured
) {
}
