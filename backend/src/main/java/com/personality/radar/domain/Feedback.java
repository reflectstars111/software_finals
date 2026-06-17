package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(indexes = {
    @Index(name = "idx_feedback_user_created", columnList = "user_id, created_at DESC")
})
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount user;

    @ManyToOne(optional = false)
    private RecommendationItem item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackRating rating;

    @Column(length = 100)
    private String comment;

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

    public RecommendationItem getItem() {
        return item;
    }

    public void setItem(RecommendationItem item) {
        this.item = item;
    }

    public FeedbackRating getRating() {
        return rating;
    }

    public void setRating(FeedbackRating rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

