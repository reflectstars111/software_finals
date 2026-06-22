package com.personality.radar.repository;

import com.personality.radar.domain.DomainTag;
import com.personality.radar.domain.Post;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByActiveTrueAndActivePostTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Post> findByActiveTrueAndActivePostTrueAndDomainTagOrderByCreatedAtDesc(DomainTag domainTag, Pageable pageable);
    List<Post> findByAuthorOrderByCreatedAtDesc(UserAccount author, Pageable pageable);
}
