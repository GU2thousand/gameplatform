package com.gamingplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "users")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private int xp;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        // Match timestamp(6) storage before returning the newly persisted entity.
        createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    @Column(unique = true, length = 64)
    private String sessionTokenHash;

    private Instant sessionExpiresAt;

    public String getSessionTokenHash() { return sessionTokenHash; }
    public void setSessionTokenHash(String value) { sessionTokenHash = value; }
    public Instant getSessionExpiresAt() { return sessionExpiresAt; }
    public void setSessionExpiresAt(Instant value) { sessionExpiresAt = value; }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getXp() {
        return xp;
    }

    public void addXp(int deltaXp) {
        this.xp += Math.max(deltaXp, 0);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
