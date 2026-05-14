package com.cobip.domain.learning;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarLearningProgressRepository extends JpaRepository<GrammarLearningProgress, Long> {

    Optional<GrammarLearningProgress> findByUserIdAndTemplateId(Long userId, Long templateId);

    @EntityGraph(attributePaths = {"template", "currentChapter"})
    List<GrammarLearningProgress> findTop5ByUserIdOrderByLastAccessedAtDesc(Long userId);

    long countByUserIdAndProgressPercentLessThan(Long userId, int progressPercent);

    long countByUserIdAndProgressPercentGreaterThanEqual(Long userId, int progressPercent);
}
