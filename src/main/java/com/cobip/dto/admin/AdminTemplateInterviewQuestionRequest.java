package com.cobip.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateInterviewQuestionRequest {

    @NotBlank(message = "Question is required.")
    @Size(max = 1000, message = "Question must be 1000 characters or less.")
    private String question;

    @Size(max = 1000, message = "Answer hint must be 1000 characters or less.")
    private String answerHint = "";
}
