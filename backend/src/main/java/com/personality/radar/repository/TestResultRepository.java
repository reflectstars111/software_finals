package com.personality.radar.repository;

import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findFirstByUserAndTypeOrderByCreatedAtDesc(UserAccount user, TestType type);
    List<TestResult> findByUserOrderByCreatedAtDesc(UserAccount user);
}

