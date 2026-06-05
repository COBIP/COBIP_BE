package com.cobip.domain.learning;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiTemplateProgressRepository extends JpaRepository<AiTemplateProgress, Long> {

    Optional<AiTemplateProgress> findByUserIdAndAiTemplateId(Long userId, String aiTemplateId);

    Page<AiTemplateProgress> findByUserIdOrderByLastAccessedAtDescIdDesc(Long userId, Pageable pageable);

    List<AiTemplateProgress> findByUserIdOrderByLastAccessedAtDesc(Long userId);

    List<AiTemplateProgress> findTop5ByUserIdOrderByLastAccessedAtDesc(Long userId);

    long countByUserIdAndProgressPercentLessThan(Long userId, int progressPercent);

    long countByUserIdAndProgressPercentGreaterThanEqual(Long userId, int progressPercent);
}
