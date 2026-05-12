package com.cobip.domain.practice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplatePracticeMissionProgressRepository
        extends JpaRepository<TemplatePracticeMissionProgress, Long> {

    Optional<TemplatePracticeMissionProgress> findByUserIdAndMissionId(Long userId, Long missionId);

    @Query("""
            select mp
            from TemplatePracticeMissionProgress mp
            join fetch mp.mission
            where mp.user.id = :userId
              and mp.mission.template.id = :templateId
            """)
    List<TemplatePracticeMissionProgress> findByUserIdAndTemplateId(
        @Param("userId") Long userId,
        @Param("templateId") Long templateId
    );

    @Query("""
            select count(mp)
            from TemplatePracticeMissionProgress mp
            where mp.user.id = :userId
              and mp.mission.template.id = :templateId
              and mp.status = :status
            """)
    long countCompletedByUserAndTemplate(
        @Param("userId") Long userId,
        @Param("templateId") Long templateId,
        @Param("status") TemplatePracticeMissionProgressStatus status
    );
}
