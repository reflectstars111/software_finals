package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ShareLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount owner;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportJson;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getReportJson() {
        return reportJson;
    }

    public void setReportJson(String reportJson) {
        this.reportJson = reportJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

