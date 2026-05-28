package com.personality.radar.repository;

import com.personality.radar.domain.AdminLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    List<AdminLog> findTop100ByOrderByCreatedAtDesc();
}

