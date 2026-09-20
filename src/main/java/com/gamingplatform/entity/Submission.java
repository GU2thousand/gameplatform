package com.gamingplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "submissions", uniqueConstraints = @jakarta.persistence.UniqueConstraint(name = "unique_submission_answer", columnNames = {"user_id", "challenge_id", "answer_hash"}))
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false, length = 10000)
    private String answer;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    void onCreate() {
        // Match timestamp(6) storage before returning the newly persisted entity.
        submittedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    @Column(name = "answer_hash", length = 64)
    private String answerHash;

    public String getAnswerHash() { return answerHash; }
    public void setAnswerHash(String value) { answerHash = value; }

    public Long getId() {
        return id;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
