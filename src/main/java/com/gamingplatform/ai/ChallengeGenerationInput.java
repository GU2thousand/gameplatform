package com.gamingplatform.ai;

import com.gamingplatform.entity.Difficulty;

import java.util.List;

public record ChallengeGenerationInput(
        Difficulty difficulty,
        String roleTrack,
        String challengeType,
        String focusGoal,
        String businessContext,
        List<String> customRequirements,
        List<String> customConstraints,
        List<String> customAcceptanceCriteria
) {

    public Difficulty resolvedDifficulty() {
        return difficulty == null ? Difficulty.INTERMEDIATE : difficulty;
    }

    public List<String> customRequirementsOrEmpty() {
        return customRequirements == null ? List.of() : customRequirements;
    }

    public List<String> customConstraintsOrEmpty() {
        return customConstraints == null ? List.of() : customConstraints;
    }

    public List<String> customAcceptanceCriteriaOrEmpty() {
        return customAcceptanceCriteria == null ? List.of() : customAcceptanceCriteria;
    }

    public boolean hasCustomPrompt() {
        return hasText(roleTrack)
                || hasText(challengeType)
                || hasText(focusGoal)
                || hasText(businessContext)
                || !customRequirementsOrEmpty().isEmpty()
                || !customConstraintsOrEmpty().isEmpty()
                || !customAcceptanceCriteriaOrEmpty().isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
