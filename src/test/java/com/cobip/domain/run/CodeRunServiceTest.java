package com.cobip.domain.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeRunServiceTest {

    @Mock
    private CodeExecutionClient codeExecutionClient;

    private CodeRunService codeRunService;

    @BeforeEach
    void setUp() {
        codeRunService = new CodeRunService(codeExecutionClient);
    }

    @Test
    void runWithInputExecutesJavascriptThroughCodeExecutionClient() {
        when(codeExecutionClient.execute(
                eq(CodingLanguage.JAVASCRIPT),
                eq("console.log(3);"),
                eq(""),
                isNull(),
                eq(3000),
                eq(128)
        )).thenReturn(result("3\n", null, null, null));

        String output = codeRunService.runWithInput("javascript", "console.log(3);", null);

        assertThat(output).isEqualTo("3");
    }

    @Test
    void runWithInputFallsBackToCompileOutput() {
        when(codeExecutionClient.execute(
                eq(CodingLanguage.JAVA),
                eq("class Main {"),
                eq(""),
                isNull(),
                eq(3000),
                eq(128)
        )).thenReturn(result(null, null, "Main.java:1: error", "Code compilation failed."));

        String output = codeRunService.runWithInput("JAVA", "class Main {", "");

        assertThat(output).isEqualTo("Main.java:1: error");
    }

    @Test
    void runWithInputRejectsUnsupportedLanguageAsInvalidRequest() {
        assertThatThrownBy(() -> codeRunService.runWithInput("ruby", "puts 1", ""))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private CodeExecutionResult result(String stdout, String stderr, String compileOutput, String message) {
        return new CodeExecutionResult(
                CodingSubmissionStatus.ACCEPTED,
                "token",
                stdout,
                stderr,
                compileOutput,
                message,
                "0.01",
                null
        );
    }
}
