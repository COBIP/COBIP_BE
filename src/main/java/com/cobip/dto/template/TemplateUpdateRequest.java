package com.cobip.dto.template;

import java.util.List;

import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TemplateUpdateRequest {

    @Size(max = 120, message = "템플릿 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    private String description;

    @Size(max = 80, message = "카테고리는 80자 이하로 입력해야 합니다.")
    private String category;

    private TemplateDifficulty difficulty;

    @Size(max = 20, message = "기술 스택은 최대 20개까지 입력할 수 있습니다.")
    private List<String> techStacks;

    private String designIntent;

    private String requirementsSpec;

    private String erd;

    private String apiSpec;

    private String projectStructure;

    private List<String> interviewQuestions;

    private TemplateVisibility visibility;

    private TemplateAccessLevel accessLevel;
}
