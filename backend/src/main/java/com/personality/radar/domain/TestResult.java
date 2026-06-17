package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(indexes = {
    @Index(name = "idx_test_result_user_type_created", columnList = "user_id, type, created_at DESC")
})
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TestType type;

    @ElementCollection
    @CollectionTable(name = "test_result_scores", joinColumns = @JoinColumn(name = "test_result_id"))
    @MapKeyColumn(name = "dimension")
    @Column(name = "score")
    private Map<String, Integer> scores = new HashMap<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public TestType getType() {
        return type;
    }

    public void setType(TestType type) {
        this.type = type;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

