package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateMissionType;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GrammarTemplateMissionRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private GrammarTemplateMissionType missionType;

    @NotNull
    private Integer orderIndex;

    private String guideContent;

    private JsonNode validationJson;
}
