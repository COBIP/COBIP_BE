package com.cobip.domain.practice;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplatePracticeProgressRepository extends JpaRepository<TemplatePracticeProgress, Long> {

    @EntityGraph(attributePaths = {"currentMission"})
    Optional<TemplatePracticeProgress> findByUserIdAndTemplateId(Long userId, Long templateId);
}
