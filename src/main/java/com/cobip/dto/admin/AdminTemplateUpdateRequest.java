package com.cobip.dto.admin;

import java.util.List;

import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateUpdateRequest {

    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @Size(max = 500, message = "Summary must be 500 characters or less.")
    private String summary;

    private String description;

    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    private TemplateDifficulty difficulty;

    @Size(max = 20, message = "Tech stacks can contain up to 20 items.")
    private List<String> techStacks;

    @Size(max = 20, message = "Tags can contain up to 20 items.")
    private List<String> tags;

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
    private List<AdminTemplateInterviewQuestionRequest> interviewQuestions;

    @Valid
    private List<AdminTemplateTestCaseRequest> testCases;

    private Boolean published;

    private TemplateVisibility visibility;

    private TemplateAccessLevel accessLevel;
}
