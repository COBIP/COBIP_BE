package com.cobip.dto.practice;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.practice.ProjectExecutionResult;
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
        this(
                submission.getId(),
                submission.getTemplate().getId(),
                submission.getMission().getId(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getPassedCount(),
                submission.getTotalCount(),
                submission.getStdout(),
                submission.getStderr(),
                submission.getCompileOutput(),
                result == null ? null : result.message(),
                result == null ? null : result.time(),
                result == null ? null : result.memory(),
                submission.getCreatedAt()
        );
    }

    private TemplatePracticeSubmissionResponse(
        Long id,
        Long templateId,
        Long missionId,
        CodingLanguage language,
        TemplatePracticeSubmissionStatus status,
        int passedCount,
        int totalCount,
        String stdout,
        String stderr,
        String compileOutput,
        String message,
        String time,
        Integer memory,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.templateId = templateId;
        this.missionId = missionId;
        this.language = language;
        this.status = status;
        this.passedCount = passedCount;
        this.totalCount = totalCount;
        this.stdout = stdout;
        this.stderr = stderr;
        this.compileOutput = compileOutput;
        this.message = message;
        this.time = time;
        this.memory = memory;
        this.createdAt = createdAt;
    }

    public static TemplatePracticeSubmissionResponse of(
        TemplatePracticeSubmission submission,
        CodeExecutionResult result
    ) {
        return new TemplatePracticeSubmissionResponse(submission, result);
    }

    public static TemplatePracticeSubmissionResponse ofProject(
        TemplatePracticeSubmission submission,
        ProjectExecutionResult result
    ) {
        return new TemplatePracticeSubmissionResponse(
                submission.getId(),
                submission.getTemplate().getId(),
                submission.getMission().getId(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getPassedCount(),
                submission.getTotalCount(),
                submission.getStdout(),
                submission.getStderr(),
                submission.getCompileOutput(),
                result.message(),
                String.valueOf(result.durationMillis()),
                null,
                submission.getCreatedAt()
        );
    }
}
