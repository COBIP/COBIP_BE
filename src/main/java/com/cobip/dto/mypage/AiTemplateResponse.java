package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.cobip.domain.learning.AiTemplateProgress;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class AiTemplateResponse {

    private final Long id;
    private final String contentType;
    private final String aiTemplateId;
    private final String templateTitle;
    private final JsonNode templateSnapshot;
    private final JsonNode sections;
    private final JsonNode lastLearningPosition;
    private final int progressPercent;
    private final String lastStep;
    private final long studySeconds;
    private final boolean completed;
    private final LocalDateTime lastAccessedAt;
    private final LocalDateTime updatedAt;

    private AiTemplateResponse(AiTemplateProgress progress) {
        this.id = progress.getId();
        this.contentType = "AI_TEMPLATE";
        this.aiTemplateId = progress.getAiTemplateId();
        this.templateTitle = progress.getTemplateTitle();
        this.templateSnapshot = progress.getTemplateSnapshotJson();
        this.sections = progress.getSectionsJson();
        this.lastLearningPosition = progress.getLastLearningPositionJson();
        this.progressPercent = progress.getProgressPercent();
        this.lastStep = progress.getLastStep();
        this.studySeconds = progress.getStudySeconds();
        this.completed = progress.isCompleted();
        this.lastAccessedAt = progress.getLastAccessedAt();
        this.updatedAt = progress.getUpdatedAt();
    }

    public static AiTemplateResponse from(AiTemplateProgress progress) {
        return new AiTemplateResponse(progress);
    }
}
