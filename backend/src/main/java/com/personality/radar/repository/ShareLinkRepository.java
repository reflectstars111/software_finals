package com.personality.radar.repository;

import com.personality.radar.domain.ShareLink;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);

    List<ShareLink> findByOwnerOrderByCreatedAtDesc(UserAccount owner);

    Optional<ShareLink> findByIdAndOwner(Long id, UserAccount owner);
}
