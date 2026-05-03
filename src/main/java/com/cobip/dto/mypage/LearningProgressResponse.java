package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.cobip.domain.learning.LearningProgress;

import lombok.Getter;

@Getter
public class LearningProgressResponse {

    private final Long templateId;
    private final String templateTitle;
    private final String thumbnailUrl;
    private final int progressPercent;
    private final String lastStep;
    private final int solvedCount;
    private final int correctCount;
    private final long studySeconds;
    private final LocalDateTime lastAccessedAt;
    private final boolean completed;

    private LearningProgressResponse(LearningProgress progress) {
        this.templateId = progress.getTemplate().getId();
        this.templateTitle = progress.getTemplate().getTitle();
        this.thumbnailUrl = progress.getTemplate().getThumbnailUrl();
        this.progressPercent = progress.getProgressPercent();
        this.lastStep = progress.getLastStep();
        this.solvedCount = progress.getSolvedCount();
        this.correctCount = progress.getCorrectCount();
        this.studySeconds = progress.getStudySeconds();
        this.lastAccessedAt = progress.getLastAccessedAt();
        this.completed = progress.isCompleted();
    }

    public static LearningProgressResponse from(LearningProgress progress) {
        return new LearningProgressResponse(progress);
    }
}
