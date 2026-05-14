package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.cobip.domain.learning.GrammarLearningProgress;
import com.cobip.domain.learning.LearningProgress;

import lombok.Getter;

@Getter
public class LearningProgressResponse {

    private final Long templateId;
    private final String contentType;
    private final String templateTitle;
    private final String thumbnailUrl;
    private final Long currentChapterId;
    private final int progressPercent;
    private final String lastStep;
    private final int solvedCount;
    private final int correctCount;
    private final long studySeconds;
    private final LocalDateTime lastAccessedAt;
    private final boolean completed;

    private LearningProgressResponse(LearningProgress progress) {
        this.templateId = progress.getTemplate().getId();
        this.contentType = "TEMPLATE";
        this.templateTitle = progress.getTemplate().getTitle();
        this.thumbnailUrl = progress.getTemplate().getThumbnailUrl();
        this.currentChapterId = null;
        this.progressPercent = progress.getProgressPercent();
        this.lastStep = progress.getLastStep();
        this.solvedCount = progress.getSolvedCount();
        this.correctCount = progress.getCorrectCount();
        this.studySeconds = progress.getStudySeconds();
        this.lastAccessedAt = progress.getLastAccessedAt();
        this.completed = progress.isCompleted();
    }

    private LearningProgressResponse(GrammarLearningProgress progress) {
        this.templateId = progress.getTemplate().getId();
        this.contentType = "GRAMMAR_TEMPLATE";
        this.templateTitle = progress.getTemplate().getTitle();
        this.thumbnailUrl = null;
        this.currentChapterId = progress.getCurrentChapter() == null ? null : progress.getCurrentChapter().getId();
        this.progressPercent = progress.getProgressPercent();
        this.lastStep = progress.getLastStep();
        this.solvedCount = 0;
        this.correctCount = 0;
        this.studySeconds = progress.getStudySeconds();
        this.lastAccessedAt = progress.getLastAccessedAt();
        this.completed = progress.isCompleted();
    }

    public static LearningProgressResponse from(LearningProgress progress) {
        return new LearningProgressResponse(progress);
    }

    public static LearningProgressResponse from(GrammarLearningProgress progress) {
        return new LearningProgressResponse(progress);
    }
}
