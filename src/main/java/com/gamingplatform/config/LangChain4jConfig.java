package com.gamingplatform.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LangChain4jOpenAiProperties.class)
public class LangChain4jConfig {

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "langchain4j")
    public ChatModel langChain4jChatModel(LangChain4jOpenAiProperties properties) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("""
                    app.ai.provider=langchain4j requires an OpenAI API key.
                    Set OPENAI_API_KEY (or app.ai.langchain4j.openai.api-key).
                    """.trim());
        }

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getModelName())
                .temperature(properties.getTemperature())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .maxRetries(properties.getMaxRetries())
                .logRequests(properties.isLogRequests())
                .logResponses(properties.isLogResponses());

        if (StringUtils.hasText(properties.getBaseUrl())) {
            builder.baseUrl(properties.getBaseUrl());
        }

        return builder.build();
    }
}
