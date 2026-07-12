package com.gamingplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(nullable = false)
    private double requirementUnderstanding;

    @Column(nullable = false)
    private double logicalClarity;

    @Column(nullable = false)
    private double technicalFeasibility;

    @Column(nullable = false)
    private double edgeCaseCoverage;

    @Column(nullable = false)
    private double communicationStructure;

    @Column(nullable = false)
    private double finalScore;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String feedback;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "strengths_json", nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String strengthsJson;

    @Column(name = "improvements_json", nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String improvementsJson;

    @Column(name = "example_outline", nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String exampleOutline;

    @Column(name = "rubric_weights_json", nullable = false)
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String rubricWeightsJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public double getRequirementUnderstanding() {
        return requirementUnderstanding;
    }

    public void setRequirementUnderstanding(double requirementUnderstanding) {
        this.requirementUnderstanding = requirementUnderstanding;
    }

    public double getLogicalClarity() {
        return logicalClarity;
    }

    public void setLogicalClarity(double logicalClarity) {
        this.logicalClarity = logicalClarity;
    }

    public double getTechnicalFeasibility() {
        return technicalFeasibility;
    }

    public void setTechnicalFeasibility(double technicalFeasibility) {
        this.technicalFeasibility = technicalFeasibility;
    }

    public double getEdgeCaseCoverage() {
        return edgeCaseCoverage;
    }

    public void setEdgeCaseCoverage(double edgeCaseCoverage) {
        this.edgeCaseCoverage = edgeCaseCoverage;
    }

    public double getCommunicationStructure() {
        return communicationStructure;
    }

    public void setCommunicationStructure(double communicationStructure) {
        this.communicationStructure = communicationStructure;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getProvider() { return provider; }

    public void setProvider(String provider) { this.provider = provider; }

    public String getStrengthsJson() { return strengthsJson; }

    public void setStrengthsJson(String strengthsJson) { this.strengthsJson = strengthsJson; }

    public String getImprovementsJson() { return improvementsJson; }

    public void setImprovementsJson(String improvementsJson) { this.improvementsJson = improvementsJson; }

    public String getExampleOutline() { return exampleOutline; }

    public void setExampleOutline(String exampleOutline) { this.exampleOutline = exampleOutline; }

    public String getRubricWeightsJson() { return rubricWeightsJson; }

    public void setRubricWeightsJson(String rubricWeightsJson) { this.rubricWeightsJson = rubricWeightsJson; }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
