package com.personality.radar.repository;

import com.personality.radar.domain.ShareLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);
}

