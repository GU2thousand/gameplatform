package com.gamingplatform.dto;

import com.gamingplatform.entity.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class ChallengeGenerateRequest {

    private Difficulty difficulty = Difficulty.INTERMEDIATE;
    @jakarta.validation.constraints.Size(max = 120)
    private String roleTrack;
    @jakarta.validation.constraints.Size(max = 120)
    private String challengeType;
    @jakarta.validation.constraints.Size(max = 300)
    private String focusGoal;
    @jakarta.validation.constraints.Size(max = 1000)
    private String businessContext;
    @jakarta.validation.constraints.Size(max = 12)
    private List<@jakarta.validation.constraints.Size(max = 400) String> customRequirements = new ArrayList<>();
    @jakarta.validation.constraints.Size(max = 12)
    private List<@jakarta.validation.constraints.Size(max = 400) String> customConstraints = new ArrayList<>();
    @jakarta.validation.constraints.Size(max = 12)
    private List<@jakarta.validation.constraints.Size(max = 400) String> customAcceptanceCriteria = new ArrayList<>();

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public String getRoleTrack() {
        return roleTrack;
    }

    public void setRoleTrack(String roleTrack) {
        this.roleTrack = normalizeText(roleTrack);
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = normalizeText(challengeType);
    }

    public String getFocusGoal() {
        return focusGoal;
    }

    public void setFocusGoal(String focusGoal) {
        this.focusGoal = normalizeText(focusGoal);
    }

    public String getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(String businessContext) {
        this.businessContext = normalizeText(businessContext);
    }

    public List<String> getCustomRequirements() {
        return customRequirements;
    }

    public void setCustomRequirements(List<String> customRequirements) {
        this.customRequirements = sanitizeList(customRequirements);
    }

    public List<String> getCustomConstraints() {
        return customConstraints;
    }

    public void setCustomConstraints(List<String> customConstraints) {
        this.customConstraints = sanitizeList(customConstraints);
    }

    public List<String> getCustomAcceptanceCriteria() {
        return customAcceptanceCriteria;
    }

    public void setCustomAcceptanceCriteria(List<String> customAcceptanceCriteria) {
        this.customAcceptanceCriteria = sanitizeList(customAcceptanceCriteria);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> sanitizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> sanitized = new ArrayList<>();
        for (String value : values) {
            String normalized = normalizeText(value);
            if (normalized != null) {
                sanitized.add(normalized);
            }
        }
        return sanitized;
    }
}
