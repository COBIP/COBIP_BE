package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplatePracticeFileType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplatePracticeFileRequest {

    @NotNull
    private GrammarTemplatePracticeFileType nodeType;

    @NotBlank
    @Size(max = 255)
    private String filePath;

    @Size(max = 40)
    private String language;

    private String content;

    @NotNull
    private Boolean readOnly;

    @NotNull
    private Integer orderIndex;
}
