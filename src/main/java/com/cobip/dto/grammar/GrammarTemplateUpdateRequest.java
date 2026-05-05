package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplateUpdateRequest {

    @Size(max = 120, message = "문법 템플릿 slug는 120자 이하로 입력해야 합니다.")
    private String slug;

    @Size(max = 120, message = "문법 템플릿 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    private GrammarTemplateLanguage language;

    @Size(max = 80, message = "카테고리는 80자 이하로 입력해야 합니다.")
    private String category;

    private GrammarTemplateDifficulty difficulty;

    @Size(max = 500, message = "요약은 500자 이하로 입력해야 합니다.")
    private String summary;

    private JsonNode contentJson;
}
