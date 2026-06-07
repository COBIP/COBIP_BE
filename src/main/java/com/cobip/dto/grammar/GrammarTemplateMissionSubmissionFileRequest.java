package com.cobip.dto.grammar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GrammarTemplateMissionSubmissionFileRequest {

    @NotBlank
    @Size(max = 255)
    private String filePath;

    @NotNull
    @Size(max = 20000)
    private String content;
}
