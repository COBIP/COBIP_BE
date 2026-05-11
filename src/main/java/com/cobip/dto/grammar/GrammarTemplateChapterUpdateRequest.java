package com.cobip.dto.grammar;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplateChapterUpdateRequest {

    @Size(max = 120)
    private String title;

    private Integer orderIndex;

    private JsonNode contentJson;
}
