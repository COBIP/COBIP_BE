package com.cobip.domain.practice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplatePracticeSubmissionRepository extends JpaRepository<TemplatePracticeSubmission, Long> {

    @Query("""
            select s
            from TemplatePracticeSubmission s
            join fetch s.mission
            where s.user.id = :userId
              and s.template.id = :templateId
            order by s.createdAt desc, s.id desc
            """)
    List<TemplatePracticeSubmission> findByUserIdAndTemplateIdOrderByCreatedAtDescIdDesc(
        @Param("userId") Long userId,
        @Param("templateId") Long templateId
    );
}
