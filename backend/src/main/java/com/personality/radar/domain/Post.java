package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(indexes = {
    @Index(name = "idx_post_author_created", columnList = "author_id, created_at DESC"),
    @Index(name = "idx_post_active_created", columnList = "active, active_post, created_at DESC")
})
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "JSON")
    private String images = "[]";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DomainTag domainTag;

    @Column(columnDefinition = "JSON")
    private String styleTags = "[]";

    @ElementCollection
    @CollectionTable(name = "post_ai_vector", joinColumns = @JoinColumn(name = "post_id"))
    @MapKeyColumn(name = "dimension")
    @Column(name = "score")
    private Map<String, Integer> aiVector = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReviewStatus aiReviewStatus = ReviewStatus.PENDING;

    @Column(length = 200)
    private String aiReviewReason;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int favoriteCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean activePost = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public enum ReviewStatus { PENDING, APPROVED, REJECTED }

    public Long getId() { return id; }

    public UserAccount getAuthor() { return author; }
    public void setAuthor(UserAccount author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public DomainTag getDomainTag() { return domainTag; }
    public void setDomainTag(DomainTag domainTag) { this.domainTag = domainTag; }

    public String getStyleTags() { return styleTags; }
    public void setStyleTags(String styleTags) { this.styleTags = styleTags; }

    public Map<String, Integer> getAiVector() { return aiVector; }
    public void setAiVector(Map<String, Integer> aiVector) { this.aiVector = aiVector; }

    public ReviewStatus getAiReviewStatus() { return aiReviewStatus; }
    public void setAiReviewStatus(ReviewStatus aiReviewStatus) { this.aiReviewStatus = aiReviewStatus; }

    public String getAiReviewReason() { return aiReviewReason; }
    public void setAiReviewReason(String aiReviewReason) { this.aiReviewReason = aiReviewReason; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isActivePost() { return activePost; }
    public void setActivePost(boolean activePost) { this.activePost = activePost; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
