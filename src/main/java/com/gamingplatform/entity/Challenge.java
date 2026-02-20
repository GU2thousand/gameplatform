package com.gamingplatform.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false, length = 2000)
    private String context;

    @ElementCollection
    @CollectionTable(name = "challenge_requirements", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "requirement", nullable = false, length = 500)
    private List<String> requirements = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "challenge_constraints", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "constraint_text", nullable = false, length = 500)
    private List<String> constraints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "challenge_acceptance_criteria", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "criterion", nullable = false, length = 500)
    private List<String> acceptanceCriteria = new ArrayList<>();

    @Column(nullable = false, length = 64)
    private String expectedOutputFormat;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public List<String> getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(List<String> acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getExpectedOutputFormat() {
        return expectedOutputFormat;
    }

    public void setExpectedOutputFormat(String expectedOutputFormat) {
        this.expectedOutputFormat = expectedOutputFormat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
