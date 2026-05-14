package com.cobip.domain.grammar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateCodeRunResponse;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.ExecutionFlowStep;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.OutputSnapshot;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.VariableSnapshot;
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
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
            "^(?:final\\s+)?(?:(int|long|double|float|boolean|char|String|var|let|const)\\s+)?"
                    + "([A-Za-z_$][A-Za-z0-9_$]*)\\s*=\\s*(.+?)\\s*;?$"
    );
    private static final Pattern JAVA_OUTPUT_PATTERN = Pattern.compile("^System\\.out\\.print(?:ln)?\\((.*)\\)\\s*;?$");
    private static final Pattern JAVASCRIPT_OUTPUT_PATTERN = Pattern.compile("^console\\.log\\((.*)\\)\\s*;?$");
    private static final Pattern PYTHON_OUTPUT_PATTERN = Pattern.compile("^print\\((.*)\\)\\s*$");

    private final GrammarTemplateRepository grammarTemplateRepository;
    private final GrammarTemplateChapterRepository grammarTemplateChapterRepository;
    private final CodeExecutionClient codeExecutionClient;
    private final JavaRuntimeExecutionFlowTracer javaRuntimeExecutionFlowTracer = new JavaRuntimeExecutionFlowTracer();

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
        if (request.getLanguage() == CodingLanguage.JAVA) {
            List<ExecutionFlowStep> runtimeSteps = buildJavaRuntimeFlow(request.getSourceCode());
            if (!runtimeSteps.isEmpty()) {
                return new GrammarTemplateExecutionFlowResponse(
                        templateId,
                        chapterId,
                        request.getLanguage(),
                        "JAVA_RUNTIME_TRACE",
                        "Runtime trace captured from instrumented Java execution.",
                        runtimeSteps
                );
            }
        }
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

    private List<ExecutionFlowStep> buildJavaRuntimeFlow(String sourceCode) {
        CodeExecutionResult result = codeExecutionClient.execute(
                CodingLanguage.JAVA,
                javaRuntimeExecutionFlowTracer.instrument(sourceCode),
                "",
                null,
                DEFAULT_TIME_LIMIT_MILLIS,
                DEFAULT_MEMORY_LIMIT_MB
        );
        if (result == null || result.status() == CodingSubmissionStatus.COMPILE_ERROR
                || result.status() == CodingSubmissionStatus.INTERNAL_ERROR) {
            return List.of();
        }
        return javaRuntimeExecutionFlowTracer.buildSteps(sourceCode, result.stdout());
    }

    private List<ExecutionFlowStep> buildStaticLineFlow(CodingLanguage language, String sourceCode) {
        String[] lines = sourceCode.split("\\R", -1);
        List<ExecutionFlowStep> steps = new ArrayList<>();
        Map<String, VariableSnapshot> variables = new LinkedHashMap<>();
        List<OutputSnapshot> outputs = new ArrayList<>();
        for (int index = 0; index < lines.length; index++) {
            String sourceLine = lines[index];
            if (sourceLine.isBlank()) {
                continue;
            }
            int stepOrder = steps.size() + 1;
            int lineNumber = index + 1;
            String eventType = eventType(language, sourceLine);
            VariableSnapshot activeVariable = parseAssignment(language, sourceLine)
                    .map(parsedVariable -> {
                        String changeType = variables.containsKey(parsedVariable.name()) ? "UPDATED" : "CREATED";
                        return new VariableSnapshot(
                                parsedVariable.name(),
                                resolveExpression(parsedVariable.expression(), variables),
                                parsedVariable.expression(),
                                parsedVariable.dataType(),
                                changeType,
                                lineNumber,
                                stepOrder
                        );
                    })
                    .orElse(null);
            if (activeVariable != null) {
                variables.put(activeVariable.getName(), activeVariable);
            }

            OutputSnapshot activeOutput = parseOutputExpression(language, sourceLine)
                    .map(expression -> new OutputSnapshot(
                            resolveExpression(expression, variables),
                            expression,
                            lineNumber,
                            stepOrder
                    ))
                    .orElse(null);
            if (activeOutput != null) {
                outputs.add(activeOutput);
            }

            steps.add(new ExecutionFlowStep(
                    stepOrder,
                    lineNumber,
                    sourceLine,
                    eventType,
                    description(eventType),
                    activeVariable,
                    activeOutput,
                    new ArrayList<>(variables.values()),
                    outputs
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

    private String description(String eventType) {
        return switch (eventType) {
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

    private Optional<ParsedVariable> parseAssignment(CodingLanguage language, String sourceLine) {
        String line = sourceLine.strip();
        if (!isAssignmentLine(language, line)) {
            return Optional.empty();
        }
        Matcher matcher = ASSIGNMENT_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String dataType = matcher.group(1);
        String name = matcher.group(2);
        String expression = cleanExpression(matcher.group(3));
        if (expression.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ParsedVariable(name, expression, dataType));
    }

    private Optional<String> parseOutputExpression(CodingLanguage language, String sourceLine) {
        Pattern outputPattern = switch (language) {
            case PYTHON -> PYTHON_OUTPUT_PATTERN;
            case JAVASCRIPT -> JAVASCRIPT_OUTPUT_PATTERN;
            case JAVA -> JAVA_OUTPUT_PATTERN;
        };
        Matcher matcher = outputPattern.matcher(sourceLine.strip());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(cleanExpression(matcher.group(1)));
    }

    private String resolveExpression(String expression, Map<String, VariableSnapshot> variables) {
        String cleanedExpression = cleanExpression(expression);
        if (cleanedExpression.isBlank()) {
            return "";
        }
        if (isQuoted(cleanedExpression)) {
            return cleanedExpression.substring(1, cleanedExpression.length() - 1);
        }
        VariableSnapshot variable = variables.get(cleanedExpression);
        if (variable != null) {
            return variable.getValue();
        }
        List<String> plusParts = splitExpression(cleanedExpression, '+');
        if (plusParts.size() > 1) {
            return resolvePlusExpression(plusParts, variables);
        }
        List<String> commaParts = splitExpression(cleanedExpression, ',');
        if (commaParts.size() > 1) {
            return resolveCommaExpression(commaParts, variables);
        }
        return cleanedExpression;
    }

    private String resolvePlusExpression(List<String> parts, Map<String, VariableSnapshot> variables) {
        List<String> values = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean allNumbers = true;
        for (String part : parts) {
            String value = resolveExpression(part, variables);
            values.add(value);
            Optional<BigDecimal> number = parseNumber(value);
            if (number.isPresent()) {
                total = total.add(number.get());
            } else {
                allNumbers = false;
            }
        }
        if (allNumbers && !values.isEmpty()) {
            return formatNumber(total);
        }
        return String.join("", values);
    }

    private String resolveCommaExpression(List<String> parts, Map<String, VariableSnapshot> variables) {
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            values.add(resolveExpression(part, variables));
        }
        return String.join(" ", values);
    }

    private Optional<BigDecimal> parseNumber(String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private String formatNumber(BigDecimal number) {
        BigDecimal normalizedNumber = number.stripTrailingZeros();
        if (normalizedNumber.scale() <= 0) {
            return normalizedNumber.toBigIntegerExact().toString();
        }
        return normalizedNumber.toPlainString();
    }

    private String cleanExpression(String expression) {
        String cleanedExpression = expression.strip();
        if (cleanedExpression.endsWith(";")) {
            return cleanedExpression.substring(0, cleanedExpression.length() - 1).strip();
        }
        return cleanedExpression;
    }

    private boolean isQuoted(String value) {
        if (value.length() < 2) {
            return false;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        return (first == '"' && last == '"') || (first == '\'' && last == '\'') || (first == '`' && last == '`');
    }

    private List<String> splitExpression(String expression, char delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        char quote = 0;
        for (int index = 0; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (isQuoteBoundary(expression, index, quote)) {
                quote = quote == 0 ? current : 0;
            }
            if (current == delimiter && quote == 0) {
                parts.add(part.toString());
                part.setLength(0);
                continue;
            }
            part.append(current);
        }
        parts.add(part.toString());
        return parts;
    }

    private boolean isQuoteBoundary(String expression, int index, char quote) {
        char current = expression.charAt(index);
        if (current != '"' && current != '\'' && current != '`') {
            return false;
        }
        if (index > 0 && expression.charAt(index - 1) == '\\') {
            return false;
        }
        return quote == 0 || quote == current;
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input;
    }

    private record ParsedVariable(String name, String expression, String dataType) {
    }
}
