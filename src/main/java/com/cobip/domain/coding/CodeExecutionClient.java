package com.cobip.domain.coding;

public interface CodeExecutionClient {

    CodeExecutionResult execute(
            CodingLanguage language,
            String sourceCode,
            String input,
            String expectedOutput,
            int timeLimitMillis,
            int memoryLimitMb
    );
}
