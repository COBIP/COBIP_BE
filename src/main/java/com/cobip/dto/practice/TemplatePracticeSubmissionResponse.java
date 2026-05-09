package com.cobip.dto.practice;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.practice.TemplatePracticeSubmission;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;

import lombok.Getter;

@Getter
public class TemplatePracticeSubmissionResponse {

    private final Long id;
    private final Long templateId;
    private final Long missionId;
    private final CodingLanguage language;
    private final TemplatePracticeSubmissionStatus status;
    private final int passedCount;
    private final int totalCount;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final String time;
    private final Integer memory;
    private final LocalDateTime createdAt;

    private TemplatePracticeSubmissionResponse(TemplatePracticeSubmission submission, CodeExecutionResult result) {
        this.id = submission.getId();
        this.templateId = submission.getTemplate().getId();
        this.missionId = submission.getMission().getId();
        this.language = submission.getLanguage();
        this.status = submission.getStatus();
        this.passedCount = submission.getPassedCount();
        this.totalCount = submission.getTotalCount();
        this.stdout = submission.getStdout();
        this.stderr = submission.getStderr();
        this.compileOutput = submission.getCompileOutput();
        this.message = result == null ? null : result.message();
        this.time = result == null ? null : result.time();
        this.memory = result == null ? null : result.memory();
        this.createdAt = submission.getCreatedAt();
    }

    public static TemplatePracticeSubmissionResponse of(
        TemplatePracticeSubmission submission,
        CodeExecutionResult result
    ) {
        return new TemplatePracticeSubmissionResponse(submission, result);
    }
}
