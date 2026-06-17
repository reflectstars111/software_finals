package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class MatchInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @ManyToOne(optional = false)
    private UserAccount owner;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MatchInviteStatus status = MatchInviteStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    public enum MatchInviteStatus {
        ACTIVE, USED, REVOKED
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public MatchInviteStatus getStatus() {
        return status;
    }

    public void setStatus(MatchInviteStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public static String generateCode() {
        return "ME" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
