package com.cobip.dto.practice;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TemplatePracticeQuizSubmissionRequest {

    @NotBlank
    private String answer;
}
