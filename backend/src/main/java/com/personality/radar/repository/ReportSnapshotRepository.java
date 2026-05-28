package com.personality.radar.repository;

import com.personality.radar.domain.ReportSnapshot;
import com.personality.radar.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, Long> {
    List<ReportSnapshot> findByOwnerOrderByCreatedAtDesc(UserAccount owner);

    Optional<ReportSnapshot> findByIdAndOwner(Long id, UserAccount owner);
}
