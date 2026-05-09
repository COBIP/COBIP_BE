package com.cobip.dto.practice;

import java.time.LocalDateTime;

import com.cobip.domain.practice.TemplatePracticeProgress;
import com.cobip.domain.practice.TemplatePracticeProgressStatus;

import lombok.Getter;

@Getter
public class TemplatePracticeProgressResponse {

    private final Long id;
    private final TemplatePracticeProgressStatus status;
    private final int progressPercent;
    private final int completedMissionCount;
    private final Long currentMissionId;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime lastAccessedAt;

    private TemplatePracticeProgressResponse(TemplatePracticeProgress progress) {
        this.id = progress.getId();
        this.status = progress.getStatus();
        this.progressPercent = progress.getProgressPercent();
        this.completedMissionCount = progress.getCompletedMissionCount();
        this.currentMissionId = progress.getCurrentMission() == null ? null : progress.getCurrentMission().getId();
        this.startedAt = progress.getStartedAt();
        this.completedAt = progress.getCompletedAt();
        this.lastAccessedAt = progress.getLastAccessedAt();
    }

    public static TemplatePracticeProgressResponse from(TemplatePracticeProgress progress) {
        if (progress == null) {
            return null;
        }
        return new TemplatePracticeProgressResponse(progress);
    }
}
