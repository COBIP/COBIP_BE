package com.cobip.dto.admin;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateCreateRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @Size(max = 500, message = "Summary must be 500 characters or less.")
    private String summary;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Category is required.")
    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    @NotNull(message = "Difficulty is required.")
    private TemplateDifficulty difficulty;

    @Size(max = 20, message = "Tech stacks can contain up to 20 items.")
    private List<String> techStacks = new ArrayList<>();

    @Size(max = 20, message = "Tags can contain up to 20 items.")
    private List<String> tags = new ArrayList<>();

    @Size(max = 40, message = "Runtime must be 40 characters or less.")
    private String runtime;

    @Size(max = 1000, message = "Preview image must be 1000 characters or less.")
    private String previewImage;

    @Size(max = 80, message = "License must be 80 characters or less.")
    private String license;

    @Size(max = 120, message = "Source must be 120 characters or less.")
    private String source;

    private String designIntent;

    private String requirementsSpec;

    private String erd;

    private String apiSpec;

    private String projectStructure;

    @Valid
    private List<AdminTemplateInterviewQuestionRequest> interviewQuestions = new ArrayList<>();

    @Valid
    private List<AdminTemplateNextRecommendationRequest> nextRecommendations = new ArrayList<>();

    @Valid
    private List<AdminTemplateTestCaseRequest> testCases = new ArrayList<>();

    private Boolean published;

    private TemplateVisibility visibility = TemplateVisibility.PUBLIC;

    private TemplateAccessLevel accessLevel = TemplateAccessLevel.FREE;
}
