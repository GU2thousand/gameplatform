package com.gamingplatform.dto;

import com.gamingplatform.entity.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ChallengeGenerateRequest {

    private Difficulty difficulty = Difficulty.INTERMEDIATE;

    @Size(max = 80)
    private String roleTrack;

    @Size(max = 120)
    private String challengeType;

    @Size(max = 160)
    private String focusGoal;

    @Size(max = 1200)
    private String businessContext;

    @Valid
    @Size(max = 10)
    private List<@NotBlank @Size(max = 400) String> customRequirements = new ArrayList<>();

    @Valid
    @Size(max = 10)
    private List<@NotBlank @Size(max = 400) String> customConstraints = new ArrayList<>();

    @Valid
    @Size(max = 10)
    private List<@NotBlank @Size(max = 400) String> customAcceptanceCriteria = new ArrayList<>();

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
            sanitized.add(normalizeText(value));
        }
        return sanitized;
    }
}
