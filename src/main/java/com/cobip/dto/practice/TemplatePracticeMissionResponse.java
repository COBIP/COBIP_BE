package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeMission;
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

    private TemplatePracticeMissionResponse(TemplatePracticeMission mission) {
        this.id = mission.getId();
        this.title = mission.getTitle();
        this.description = mission.getDescription();
        this.missionType = mission.getMissionType();
        this.orderIndex = mission.getOrderIndex();
        this.guideContent = mission.getGuideContent();
        this.validationJson = mission.getValidationJson();
    }

    public static TemplatePracticeMissionResponse from(TemplatePracticeMission mission) {
        return new TemplatePracticeMissionResponse(mission);
    }
}
