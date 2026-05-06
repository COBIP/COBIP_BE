package com.cobip.domain.learning;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    @EntityGraph(attributePaths = {"template", "template.owner"})
    Page<LearningProgress> findByUserIdOrderByLastAccessedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"template", "template.owner"})
    List<LearningProgress> findTop5ByUserIdOrderByLastAccessedAtDesc(Long userId);

    Optional<LearningProgress> findByUserIdAndTemplateId(Long userId, Long templateId);

    long countByUserIdAndProgressPercentLessThan(Long userId, int progressPercent);

    long countByUserIdAndProgressPercentGreaterThanEqual(Long userId, int progressPercent);

    long countByProgressPercentGreaterThanEqual(int progressPercent);

    @Query("select coalesce(sum(lp.studySeconds), 0) from LearningProgress lp")
    long sumStudySeconds();

    @Query("select coalesce(sum(lp.solvedCount), 0) from LearningProgress lp")
    long sumSolvedCount();

    @Query("select coalesce(sum(lp.correctCount), 0) from LearningProgress lp")
    long sumCorrectCount();

    List<LearningProgress> findByUserId(Long userId);
}
