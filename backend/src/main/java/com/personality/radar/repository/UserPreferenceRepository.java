package com.personality.radar.repository;

import com.personality.radar.domain.UserAccount;
import com.personality.radar.domain.UserPreference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUser(UserAccount user);
    Optional<UserPreference> findByUserAndTag(UserAccount user, String tag);
}

