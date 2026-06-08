package com.cobip.dto.grammar;

import com.cobip.domain.practice.ProjectExecutionResult;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;

import lombok.Getter;

@Getter
public class GrammarTemplateMissionSubmissionResponse {

    private final Long templateId;
    private final Long chapterId;
    private final Long missionId;
    private final TemplatePracticeSubmissionStatus status;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final int passedCount;
    private final int totalCount;

    private GrammarTemplateMissionSubmissionResponse(
        Long templateId,
        Long chapterId,
        Long missionId,
        TemplatePracticeSubmissionStatus status,
        String stdout,
        String stderr,
        String compileOutput,
        String message,
        int passedCount,
        int totalCount
    ) {
        this.templateId = templateId;
        this.chapterId = chapterId;
        this.missionId = missionId;
        this.status = status;
        this.stdout = stdout;
        this.stderr = stderr;
        this.compileOutput = compileOutput;
        this.message = message;
        this.passedCount = passedCount;
        this.totalCount = totalCount;
    }

    public static GrammarTemplateMissionSubmissionResponse ofProject(
        Long templateId,
        Long chapterId,
        Long missionId,
        ProjectExecutionResult result
    ) {
        int passedCount = result.status() == TemplatePracticeSubmissionStatus.ACCEPTED ? 1 : 0;
        return new GrammarTemplateMissionSubmissionResponse(
                templateId,
                chapterId,
                missionId,
                result.status(),
                result.stdout(),
                result.stderr(),
                null,
                result.message(),
                passedCount,
                1
        );
    }

    public static GrammarTemplateMissionSubmissionResponse ofAnswerValidation(
        Long templateId,
        Long chapterId,
        Long missionId,
        TemplatePracticeSubmissionStatus status,
        String message,
        int passedCount,
        int totalCount
    ) {
        return new GrammarTemplateMissionSubmissionResponse(
                templateId,
                chapterId,
                missionId,
                status,
                null,
                null,
                null,
                message,
                passedCount,
                totalCount
        );
    }
}
