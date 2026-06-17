package com.personality.radar.repository;

import com.personality.radar.domain.MatchInvite;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchInviteRepository extends JpaRepository<MatchInvite, Long> {
    Optional<MatchInvite> findByCode(String code);

    List<MatchInvite> findByOwnerOrderByCreatedAtDesc(UserAccount owner);

    Optional<MatchInvite> findByOwnerAndStatus(UserAccount owner, MatchInvite.MatchInviteStatus status);
}
