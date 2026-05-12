package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeMission;
import com.cobip.domain.practice.TemplatePracticeMissionProgressStatus;
import com.cobip.domain.practice.TemplatePracticeMissionType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class TemplatePracticeMissionResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final TemplatePracticeMissionType missionType;
    private final int orderIndex;
    private final String guideContent;
    private final JsonNode validationJson;
    private final TemplatePracticeMissionProgressStatus progressStatus;

    private TemplatePracticeMissionResponse(
        TemplatePracticeMission mission,
        TemplatePracticeMissionProgressStatus progressStatus
    ) {
        this.id = mission.getId();
        this.title = mission.getTitle();
        this.description = mission.getDescription();
        this.missionType = mission.getMissionType();
        this.orderIndex = mission.getOrderIndex();
        this.guideContent = mission.getGuideContent();
        this.validationJson = mission.getValidationJson();
        this.progressStatus = progressStatus;
    }

    public static TemplatePracticeMissionResponse from(TemplatePracticeMission mission) {
        return from(mission, null);
    }

    public static TemplatePracticeMissionResponse from(
        TemplatePracticeMission mission,
        TemplatePracticeMissionProgressStatus progressStatus
    ) {
        return new TemplatePracticeMissionResponse(mission, progressStatus);
    }
}
