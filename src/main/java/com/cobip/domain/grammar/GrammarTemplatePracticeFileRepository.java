package com.cobip.domain.grammar;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarTemplatePracticeFileRepository extends JpaRepository<GrammarTemplatePracticeFile, Long> {

    List<GrammarTemplatePracticeFile> findByTemplateIdOrderByChapterIdAscOrderIndexAscIdAsc(Long templateId);

    List<GrammarTemplatePracticeFile> findByTemplateIdAndChapterIdOrderByOrderIndexAscIdAsc(
        Long templateId,
        Long chapterId
    );

    Optional<GrammarTemplatePracticeFile> findByIdAndTemplateIdAndChapterId(Long id, Long templateId, Long chapterId);
}
