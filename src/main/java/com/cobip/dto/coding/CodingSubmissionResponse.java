package com.cobip.dto.coding;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmission;
import com.cobip.domain.coding.CodingSubmissionStatus;

import lombok.Getter;

@Getter
public class CodingSubmissionResponse {

    private final Long id;
    private final Long problemId;
    private final CodingLanguage language;
    private final CodingSubmissionStatus status;
    private final int passedCount;
    private final int totalCount;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final String time;
    private final Integer memory;
    private final LocalDateTime createdAt;

    private CodingSubmissionResponse(
        Long id,
        Long problemId,
        CodingLanguage language,
        CodingSubmissionStatus status,
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
        this.problemId = problemId;
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

    public static CodingSubmissionResponse of(CodingSubmission submission, CodeExecutionResult result) {
        return new CodingSubmissionResponse(
                submission.getId(),
                submission.getProblem().getId(),
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
}
