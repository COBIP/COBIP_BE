package com.cobip.dto.practice;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;

import lombok.Getter;

@Getter
public class TemplatePracticeCodeRunResponse {

    private final TemplatePracticeSubmissionStatus status;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final String time;
    private final Integer memory;

    private TemplatePracticeCodeRunResponse(CodeExecutionResult result) {
        this.status = TemplatePracticeSubmissionStatus.from(result.status());
        this.stdout = result.stdout();
        this.stderr = result.stderr();
        this.compileOutput = result.compileOutput();
        this.message = result.message();
        this.time = result.time();
        this.memory = result.memory();
    }

    public static TemplatePracticeCodeRunResponse from(CodeExecutionResult result) {
        return new TemplatePracticeCodeRunResponse(result);
    }
}
