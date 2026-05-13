package com.cobip.dto.grammar;

import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingSubmissionStatus;

import lombok.Getter;

@Getter
public class GrammarTemplateCodeRunResponse {

    private final CodingSubmissionStatus status;
    private final String stdout;
    private final String stderr;
    private final String compileOutput;
    private final String message;
    private final String time;
    private final Integer memory;

    private GrammarTemplateCodeRunResponse(CodeExecutionResult result) {
        this.status = result.status();
        this.stdout = result.stdout();
        this.stderr = result.stderr();
        this.compileOutput = result.compileOutput();
        this.message = result.message();
        this.time = result.time();
        this.memory = result.memory();
    }

    public static GrammarTemplateCodeRunResponse from(CodeExecutionResult result) {
        return new GrammarTemplateCodeRunResponse(result);
    }
}
