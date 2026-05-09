package com.cobip.domain.coding;

public record CodeExecutionResult(
        CodingSubmissionStatus status,
        String token,
        String stdout,
        String stderr,
        String compileOutput,
        String message,
        String time,
        Integer memory
) {
}
