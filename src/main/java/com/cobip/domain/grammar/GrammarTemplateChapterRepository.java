package com.cobip.domain.grammar;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarTemplateChapterRepository extends JpaRepository<GrammarTemplateChapter, Long> {

    List<GrammarTemplateChapter> findByTemplateIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(Long templateId);

    Optional<GrammarTemplateChapter> findByIdAndTemplateIdAndDeletedAtIsNull(Long id, Long templateId);
}
