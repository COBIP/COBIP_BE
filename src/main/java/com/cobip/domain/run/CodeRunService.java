package com.cobip.domain.run;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CodeRunService {

    private static final int TIME_LIMIT_MILLIS = 3000;
    private static final int MEMORY_LIMIT_MB = 128;

    private final CodeExecutionClient codeExecutionClient;

    public String runWithInput(String language, String code, String input) {
        if (code == null || code.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        CodeExecutionResult result = codeExecutionClient.execute(
                parseLanguage(language),
                code,
                input == null ? "" : input,
                null,
                TIME_LIMIT_MILLIS,
                MEMORY_LIMIT_MB
        );
        return outputText(result);
    }

    private CodingLanguage parseLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        try {
            return CodingLanguage.valueOf(language.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, e);
        }
    }

    private String outputText(CodeExecutionResult result) {
        String output = firstPresent(
                result.stdout(),
                result.compileOutput(),
                result.stderr(),
                result.message()
        );
        return output == null ? "" : output.trim();
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
