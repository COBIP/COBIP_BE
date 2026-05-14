package com.cobip.domain.grammar;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateCodeRunResponse;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.ExecutionFlowStep;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GrammarTemplateExecutionService {

    private static final int DEFAULT_TIME_LIMIT_MILLIS = 5000;
    private static final int DEFAULT_MEMORY_LIMIT_MB = 128;

    private final GrammarTemplateRepository grammarTemplateRepository;
    private final GrammarTemplateChapterRepository grammarTemplateChapterRepository;
    private final CodeExecutionClient codeExecutionClient;

    @Transactional(readOnly = true)
    public GrammarTemplateCodeRunResponse runChapter(
        Long templateId,
        Long chapterId,
        GrammarTemplateCodeRunRequest request
    ) {
        validatePublishedChapter(templateId, chapterId);
        CodeExecutionResult result = codeExecutionClient.execute(
                request.getLanguage(),
                request.getSourceCode(),
                normalizeInput(request.getInput()),
                null,
                DEFAULT_TIME_LIMIT_MILLIS,
                DEFAULT_MEMORY_LIMIT_MB
        );
        return GrammarTemplateCodeRunResponse.from(result);
    }

    @Transactional(readOnly = true)
    public GrammarTemplateExecutionFlowResponse getExecutionFlow(
        Long templateId,
        Long chapterId,
        GrammarTemplateExecutionFlowRequest request
    ) {
        validatePublishedChapter(templateId, chapterId);
        return new GrammarTemplateExecutionFlowResponse(
                templateId,
                chapterId,
                request.getLanguage(),
                buildStaticLineFlow(request.getLanguage(), request.getSourceCode())
        );
    }

    private void validatePublishedChapter(Long templateId, Long chapterId) {
        grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(templateId, GrammarTemplateStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND));
        grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(chapterId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_CHAPTER_NOT_FOUND));
    }

    private List<ExecutionFlowStep> buildStaticLineFlow(CodingLanguage language, String sourceCode) {
        String[] lines = sourceCode.split("\\R", -1);
        List<ExecutionFlowStep> steps = new ArrayList<>();
        for (int index = 0; index < lines.length; index++) {
            String sourceLine = lines[index];
            if (sourceLine.isBlank()) {
                continue;
            }
            steps.add(new ExecutionFlowStep(
                    steps.size() + 1,
                    index + 1,
                    sourceLine,
                    eventType(language, sourceLine),
                    description(language, sourceLine)
            ));
        }
        return steps;
    }

    private String eventType(CodingLanguage language, String sourceLine) {
        String line = sourceLine.strip();
        if (isOutputLine(language, line)) {
            return "OUTPUT";
        }
        if (isConditionLine(line)) {
            return "CONDITION";
        }
        if (isLoopLine(line)) {
            return "LOOP";
        }
        if (isFunctionLine(language, line)) {
            return "FUNCTION";
        }
        if (isAssignmentLine(language, line)) {
            return "ASSIGNMENT";
        }
        return "LINE";
    }

    private String description(CodingLanguage language, String sourceLine) {
        return switch (eventType(language, sourceLine)) {
            case "OUTPUT" -> "Execute output statement.";
            case "CONDITION" -> "Evaluate branch condition.";
            case "LOOP" -> "Evaluate loop statement.";
            case "FUNCTION" -> "Define function or method.";
            case "ASSIGNMENT" -> "Store a value in a variable.";
            default -> "Execute this line.";
        };
    }

    private boolean isOutputLine(CodingLanguage language, String line) {
        return switch (language) {
            case PYTHON -> line.startsWith("print(");
            case JAVASCRIPT -> line.startsWith("console.log(");
            case JAVA -> line.startsWith("System.out.print");
        };
    }

    private boolean isConditionLine(String line) {
        return line.startsWith("if ") || line.startsWith("if(")
                || line.startsWith("else if") || line.startsWith("elif ")
                || line.startsWith("else");
    }

    private boolean isLoopLine(String line) {
        return line.startsWith("for ") || line.startsWith("for(")
                || line.startsWith("while ") || line.startsWith("while(");
    }

    private boolean isFunctionLine(CodingLanguage language, String line) {
        return switch (language) {
            case PYTHON -> line.startsWith("def ");
            case JAVASCRIPT -> line.startsWith("function ") || line.contains("=>");
            case JAVA -> line.contains(" void ") || line.contains(" int ") || line.contains(" String ");
        };
    }

    private boolean isAssignmentLine(CodingLanguage language, String line) {
        if (!line.contains("=") || line.contains("==") || line.contains("!=")
                || line.contains("<=") || line.contains(">=")) {
            return false;
        }
        return switch (language) {
            case PYTHON -> true;
            case JAVASCRIPT -> line.startsWith("const ") || line.startsWith("let ") || line.startsWith("var ")
                    || line.matches("[A-Za-z_$][A-Za-z0-9_$]*\\s*=.*");
            case JAVA -> line.matches(".*\\b[A-Za-z_$][A-Za-z0-9_$]*\\s*=.*");
        };
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input;
    }
}
