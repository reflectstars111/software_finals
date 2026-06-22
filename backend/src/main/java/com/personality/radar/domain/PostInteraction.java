package com.personality.radar.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id", "type"}),
       indexes = @Index(name = "idx_pi_post_type", columnList = "post_id, type"))
public class PostInteraction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount user;

    @ManyToOne(optional = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InteractionType type;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public enum InteractionType { LIKE, FAVORITE }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public InteractionType getType() { return type; }
    public void setType(InteractionType type) { this.type = type; }
    public Instant getCreatedAt() { return createdAt; }
}
