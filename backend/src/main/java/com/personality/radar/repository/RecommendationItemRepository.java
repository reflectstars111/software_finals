package com.personality.radar.repository;

import com.personality.radar.domain.RecommendationItem;
import com.personality.radar.domain.SceneType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {
    List<RecommendationItem> findBySceneAndActiveTrue(SceneType scene);
}

