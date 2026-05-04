package com.cobip.dto.template;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TemplateCreateRequest {

    @NotBlank(message = "템플릿 제목은 필수입니다.")
    @Size(max = 120, message = "템플릿 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    @NotBlank(message = "템플릿 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 80, message = "카테고리는 80자 이하로 입력해야 합니다.")
    private String category;

    @NotNull(message = "난이도는 필수입니다.")
    private TemplateDifficulty difficulty;

    @Size(max = 20, message = "기술 스택은 최대 20개까지 입력할 수 있습니다.")
    private List<String> techStacks = new ArrayList<>();

    private String designIntent;

    private String requirementsSpec;

    private String erd;

    private String apiSpec;

    private String projectStructure;

    private List<String> interviewQuestions = new ArrayList<>();

    @NotNull(message = "공개 여부는 필수입니다.")
    private TemplateVisibility visibility = TemplateVisibility.PUBLIC;

    @NotNull(message = "접근 등급은 필수입니다.")
    private TemplateAccessLevel accessLevel = TemplateAccessLevel.FREE;
}
