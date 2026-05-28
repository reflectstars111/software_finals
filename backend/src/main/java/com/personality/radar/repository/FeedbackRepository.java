package com.personality.radar.repository;

import com.personality.radar.domain.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop100ByOrderByCreatedAtDesc();
}

