package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplateCreateRequest {

    @NotBlank(message = "문법 템플릿 slug는 필수입니다.")
    @Size(max = 120, message = "문법 템플릿 slug는 120자 이하로 입력해야 합니다.")
    private String slug;

    @NotBlank(message = "문법 템플릿 제목은 필수입니다.")
    @Size(max = 120, message = "문법 템플릿 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    @NotNull(message = "언어는 필수입니다.")
    private GrammarTemplateLanguage language;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 80, message = "카테고리는 80자 이하로 입력해야 합니다.")
    private String category;

    @NotNull(message = "난이도는 필수입니다.")
    private GrammarTemplateDifficulty difficulty;

    @NotBlank(message = "요약은 필수입니다.")
    @Size(max = 500, message = "요약은 500자 이하로 입력해야 합니다.")
    private String summary;

    @NotNull(message = "문법 템플릿 내용은 필수입니다.")
    private JsonNode contentJson;

    private GrammarTemplateStatus status = GrammarTemplateStatus.DRAFT;
}
