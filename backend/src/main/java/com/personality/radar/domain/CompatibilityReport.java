package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class CompatibilityReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount owner;

    @ManyToOne(optional = false)
    private UserAccount target;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(nullable = false, length = 500)
    private String advantages;

    @Column(nullable = false, length = 500)
    private String warnings;

    @Column(nullable = false, length = 500)
    private String advice;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public UserAccount getTarget() {
        return target;
    }

    public void setTarget(UserAccount target) {
        this.target = target;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAdvantages() {
        return advantages;
    }

    public void setAdvantages(String advantages) {
        this.advantages = advantages;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

