package com.personality.radar.repository;

import com.personality.radar.domain.Post;
import com.personality.radar.domain.PostInteraction;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostInteractionRepository extends JpaRepository<PostInteraction, Long> {
    Optional<PostInteraction> findByUserAndPostAndType(UserAccount user, Post post, PostInteraction.InteractionType type);
    long countByPostAndType(Post post, PostInteraction.InteractionType type);
    List<PostInteraction> findByPostAndType(Post post, PostInteraction.InteractionType type);
    List<PostInteraction> findByUserAndTypeOrderByCreatedAtDesc(UserAccount user, PostInteraction.InteractionType type, Pageable pageable);
    void deleteByUserAndPostInAndType(UserAccount user, List<Post> posts, PostInteraction.InteractionType type);
}
