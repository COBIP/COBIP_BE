package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeMissionType;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TemplatePracticeMissionRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private TemplatePracticeMissionType missionType;

    @NotNull
    private Integer orderIndex;

    private String guideContent;

    private JsonNode validationJson;
}
