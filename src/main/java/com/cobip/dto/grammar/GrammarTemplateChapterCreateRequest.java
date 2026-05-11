package com.cobip.dto.grammar;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplateChapterCreateRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotNull
    private Integer orderIndex;

    @NotNull
    private JsonNode contentJson;
}
