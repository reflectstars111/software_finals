package com.personality.radar.repository;

import com.personality.radar.domain.Question;
import com.personality.radar.domain.TestType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTypeAndActiveTrueOrderBySortOrderAsc(TestType type);
}

