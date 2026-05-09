package com.cobip.domain.practice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplatePracticeMissionRepository extends JpaRepository<TemplatePracticeMission, Long> {

    List<TemplatePracticeMission> findByTemplateIdOrderByOrderIndexAscIdAsc(Long templateId);

    Optional<TemplatePracticeMission> findByIdAndTemplateId(Long id, Long templateId);

    Optional<TemplatePracticeMission> findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(
        Long templateId,
        int orderIndex
    );

    long countByTemplateId(Long templateId);
}
