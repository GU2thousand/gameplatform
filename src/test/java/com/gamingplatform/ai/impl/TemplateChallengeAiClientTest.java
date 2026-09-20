package com.gamingplatform.ai.impl;

import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateChallengeAiClientTest {

    private final TemplateChallengeAiClient client = new TemplateChallengeAiClient();

    @Test
    void customBriefUsesOnlyTheRequestedScenarioAcrossRepeatedGenerations() {
        var input = new ChallengeGenerationInput(Difficulty.INTERMEDIATE, "Product Manager", "PRD",
                "Reduce appointment no-shows", "A dental clinic needs an appointment booking workflow.",
                List.of("Support rescheduling."), List.of("Do not store clinical records."),
                List.of("Measure attendance after launch."));

        GeneratedChallenge first = client.generate(input);
        assertThat(client.generate(input)).isEqualTo(first);
        assertThat(first.title()).contains("PRD", "dental clinic");
        assertThat(first.context()).contains(input.businessContext(), input.roleTrack(), input.focusGoal());
        assertThat(first.requirements()).contains("Support rescheduling.");
        assertThat(first.constraints()).contains("Do not store clinical records.");
        assertThat(first.acceptanceCriteria()).contains("Measure attendance after launch.");
        assertThat(first.toString()).doesNotContain("ranked queue", "notification", "gaming", "cross-region", "50k");
    }

    @Test
    void listOnlyCustomizationStillProducesACompleteIndependentBrief() {
        GeneratedChallenge result = client.generate(new ChallengeGenerationInput(null, null, null, null, null,
                List.of(" Create a museum exhibit plan. ", "Create a museum exhibit plan."), null, null));

        assertThat(result.difficulty()).isEqualTo(Difficulty.INTERMEDIATE);
        assertThat(result.context()).contains("supplied requirements");
        assertThat(result.requirements()).containsOnlyOnce("Create a museum exhibit plan.");
        assertThat(result.constraints()).isNotEmpty();
        assertThat(result.acceptanceCriteria()).isNotEmpty();
        assertThat(result.toString()).doesNotContain("notification", "ranked queue", "game telemetry");
    }

    @Test
    void maximumValidInputsFitPersistenceColumnsAndPreserveCustomLists() {
        List<String> customItems = IntStream.range(0, 12).mapToObj(i -> i + "x".repeat(398)).toList();
        var input = new ChallengeGenerationInput(Difficulty.ADVANCED, "r".repeat(120), "t".repeat(120),
                "f".repeat(300), "c".repeat(1000), customItems, customItems, customItems);

        GeneratedChallenge result = client.generate(input);

        assertThat(result.title().length()).isLessThanOrEqualTo(255);
        assertThat(result.context().length()).isLessThanOrEqualTo(2000);
        assertThat(result.context()).contains(input.businessContext(), input.roleTrack(), input.challengeType(), input.focusGoal());
        assertThat(result.expectedOutputFormat().length()).isLessThanOrEqualTo(64);
        Stream.of(result.requirements(), result.constraints(), result.acceptanceCriteria()).forEach(section -> {
            assertThat(section).containsAll(customItems);
            assertThat(section).allSatisfy(item -> assertThat(item.length()).isLessThanOrEqualTo(500));
        });
    }

    @Test
    void uncustomizedRequestsContinueToUseTheDifficultyBank() {
        GeneratedChallenge result = client.generate(new ChallengeGenerationInput(Difficulty.BEGINNER,
                null, null, null, null, null, null, null));

        assertThat(result.difficulty()).isEqualTo(Difficulty.BEGINNER);
        assertThat(result.title()).isEqualTo("Design a Basic Notification Retry Policy");
        assertThat(client.generate(null).requirements()).isNotEmpty();
    }
}
