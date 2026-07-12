package com.gamingplatform.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.entity.Difficulty;
import com.gamingplatform.exception.InvalidAiOutputException;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LangChain4jChallengeAiClientTest {

    @Test
    void shouldRejectDifficultyThatDoesNotMatchTheRequest() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat(anyString())).thenReturn("""
                {
                  "title": "Unexpected challenge",
                  "difficulty": "ADVANCED",
                  "context": "A realistic context",
                  "requirements": ["Define the API"],
                  "constraints": ["Keep latency low"],
                  "acceptanceCriteria": ["Explain tradeoffs"],
                  "expectedOutputFormat": "Markdown"
                }
                """);
        LangChain4jChallengeAiClient client = new LangChain4jChallengeAiClient(chatModel, new ObjectMapper());
        ChallengeGenerationInput input = new ChallengeGenerationInput(
                Difficulty.BEGINNER, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> client.generate(input))
                .isInstanceOf(InvalidAiOutputException.class)
                .hasMessageContaining("invalid JSON");
    }
}
