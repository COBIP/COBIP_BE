package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;

import lombok.Getter;

@Getter
public class TemplatePracticeQuizSubmissionResponse {

    private final boolean correct;
    private final TemplatePracticeSubmissionStatus status;
    private final String message;
    private final String explanation;
    private final int progressPercent;

    private TemplatePracticeQuizSubmissionResponse(
        boolean correct,
        TemplatePracticeSubmissionStatus status,
        String explanation,
        int progressPercent
    ) {
        this.correct = correct;
        this.status = status;
        this.message = correct ? "정답입니다." : "오답입니다.";
        this.explanation = explanation;
        this.progressPercent = progressPercent;
    }

    public static TemplatePracticeQuizSubmissionResponse of(
        boolean correct,
        String explanation,
        int progressPercent
    ) {
        return new TemplatePracticeQuizSubmissionResponse(
                correct,
                correct ? TemplatePracticeSubmissionStatus.ACCEPTED : TemplatePracticeSubmissionStatus.WRONG_ANSWER,
                explanation,
                progressPercent
        );
    }
}
