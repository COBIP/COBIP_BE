package com.cobip.domain.practice;

public record ProjectExecutionResult(
    TemplatePracticeSubmissionStatus status,
    int exitCode,
    String stdout,
    String stderr,
    String message,
    long durationMillis
) {
}
