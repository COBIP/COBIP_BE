package com.cobip.domain.grammar;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarTemplateChapterMissionRepository extends JpaRepository<GrammarTemplateChapterMission, Long> {

    List<GrammarTemplateChapterMission> findByTemplateIdOrderByChapterIdAscOrderIndexAscIdAsc(Long templateId);

    List<GrammarTemplateChapterMission> findByTemplateIdAndChapterIdOrderByOrderIndexAscIdAsc(
        Long templateId,
        Long chapterId
    );

    Optional<GrammarTemplateChapterMission> findByIdAndTemplateIdAndChapterId(
        Long id,
        Long templateId,
        Long chapterId
    );
}
