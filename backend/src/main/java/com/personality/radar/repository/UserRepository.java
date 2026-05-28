package com.personality.radar.repository;

import com.personality.radar.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByPhone(String phone);
    boolean existsByPhone(String phone);
}

