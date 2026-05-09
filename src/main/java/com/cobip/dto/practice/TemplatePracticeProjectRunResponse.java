package com.cobip.dto.practice;

import com.cobip.domain.practice.ProjectExecutionResult;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;

import lombok.Getter;

@Getter
public class TemplatePracticeProjectRunResponse {

    private final TemplatePracticeSubmissionStatus status;
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final String message;
    private final long durationMillis;

    private TemplatePracticeProjectRunResponse(ProjectExecutionResult result) {
        this.status = result.status();
        this.exitCode = result.exitCode();
        this.stdout = result.stdout();
        this.stderr = result.stderr();
        this.message = result.message();
        this.durationMillis = result.durationMillis();
    }

    public static TemplatePracticeProjectRunResponse from(ProjectExecutionResult result) {
        return new TemplatePracticeProjectRunResponse(result);
    }
}
