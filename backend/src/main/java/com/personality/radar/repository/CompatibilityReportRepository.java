package com.personality.radar.repository;

import com.personality.radar.domain.CompatibilityReport;
import com.personality.radar.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompatibilityReportRepository extends JpaRepository<CompatibilityReport, Long> {
    Optional<CompatibilityReport> findByIdAndOwner(Long id, UserAccount owner);
}

