package com.cobip.domain.practice;

import com.cobip.domain.coding.CodingSubmissionStatus;

public enum TemplatePracticeSubmissionStatus {
    ACCEPTED,
    WRONG_ANSWER,
    TIME_LIMIT_EXCEEDED,
    COMPILE_ERROR,
    RUNTIME_ERROR,
    INTERNAL_ERROR;

    public static TemplatePracticeSubmissionStatus from(CodingSubmissionStatus status) {
        return TemplatePracticeSubmissionStatus.valueOf(status.name());
    }
}
