package com.personality.radar.repository;

import com.personality.radar.domain.RecommendationRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {
    List<RecommendationRule> findByActiveTrueOrderByTagAsc();

    List<RecommendationRule> findAllByOrderByTagAsc();

    Optional<RecommendationRule> findByTag(String tag);
}
