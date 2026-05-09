package com.cobip.dto.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TemplatePracticeFileRequest {

    @NotBlank
    private String filePath;

    @NotBlank
    private String language;

    @NotNull
    private String content;

    @NotNull
    private Boolean readOnly;

    @NotNull
    private Integer orderIndex;
}
