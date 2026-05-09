package com.cobip.dto.coding;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingSubmissionStatus;

import lombok.Getter;

@Getter
public class CodingCodeRunResponse {

    private final CodingSubmissionStatus status;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final String time;
    private final Integer memory;

    private CodingCodeRunResponse(
        CodingSubmissionStatus status,
        String stdout,
        String stderr,
        String compileOutput,
        String message,
        String time,
        Integer memory
    ) {
        this.status = status;
        this.stdout = stdout;
        this.stderr = stderr;
        this.compileOutput = compileOutput;
        this.message = message;
        this.time = time;
        this.memory = memory;
    }

    public static CodingCodeRunResponse from(CodeExecutionResult result) {
        return new CodingCodeRunResponse(
                result.status(),
                result.stdout(),
                result.stderr(),
                result.compileOutput(),
                result.message(),
                result.time(),
                result.memory()
        );
    }
}
