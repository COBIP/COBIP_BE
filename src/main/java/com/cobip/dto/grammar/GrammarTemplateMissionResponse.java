package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateChapterMission;
import com.cobip.domain.grammar.GrammarTemplateMissionType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class GrammarTemplateMissionResponse {

    private final Long id;
    private final Long templateId;
    private final Long chapterId;
    private final String title;
    private final String description;
    private final GrammarTemplateMissionType missionType;
    private final int orderIndex;
    private final String guideContent;
    private final JsonNode validationJson;

    private GrammarTemplateMissionResponse(GrammarTemplateChapterMission mission) {
        this.id = mission.getId();
        this.templateId = mission.getTemplate().getId();
        this.chapterId = mission.getChapter().getId();
        this.title = mission.getTitle();
        this.description = mission.getDescription();
        this.missionType = mission.getMissionType();
        this.orderIndex = mission.getOrderIndex();
        this.guideContent = mission.getGuideContent();
        this.validationJson = mission.getValidationJson();
    }

    public static GrammarTemplateMissionResponse from(GrammarTemplateChapterMission mission) {
        return new GrammarTemplateMissionResponse(mission);
    }
}
