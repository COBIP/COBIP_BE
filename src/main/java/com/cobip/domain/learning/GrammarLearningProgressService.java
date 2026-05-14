package com.cobip.domain.learning;

import java.util.List;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplateChapterRepository;
import com.cobip.domain.grammar.GrammarTemplateRepository;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.domain.user.User;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrammarLearningProgressService {

    private final GrammarTemplateRepository grammarTemplateRepository;
    private final GrammarTemplateChapterRepository grammarTemplateChapterRepository;
    private final GrammarLearningProgressRepository grammarLearningProgressRepository;

    @Transactional
    public void recordAccess(User user, Long templateId, Long chapterId, long activeSeconds) {
        if (user == null || templateId == null) {
            return;
        }

        GrammarTemplate template = grammarTemplateRepository
                .findByIdAndStatusAndDeletedAtIsNull(templateId, GrammarTemplateStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND));
        List<GrammarTemplateChapter> chapters = grammarTemplateChapterRepository
                .findByTemplateIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(templateId);
        GrammarTemplateChapter chapter = chapterId == null ? null : chapters.stream()
                .filter(candidate -> candidate.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_CHAPTER_NOT_FOUND));

        GrammarLearningProgress progress = grammarLearningProgressRepository
                .findByUserIdAndTemplateId(user.getId(), templateId)
                .orElseGet(() -> GrammarLearningProgress.start(user, template));
        progress.recordAccess(chapter, calculateProgressPercent(chapters, chapter), activeSeconds);
        grammarLearningProgressRepository.save(progress);
    }

    private int calculateProgressPercent(List<GrammarTemplateChapter> chapters, GrammarTemplateChapter chapter) {
        if (chapters.isEmpty()) {
            return 100;
        }
        if (chapter == null) {
            return 0;
        }
        int chapterIndex = chapters.indexOf(chapter);
        if (chapterIndex < 0) {
            return 0;
        }
        return Math.min(100, Math.round(((chapterIndex + 1) * 100f) / chapters.size()));
    }
}
