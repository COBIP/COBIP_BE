package com.cobip.dto.grammar;

import java.util.List;

import com.cobip.domain.coding.CodingLanguage;

import lombok.Getter;

@Getter
public class GrammarTemplateExecutionFlowResponse {

    private final Long templateId;
    private final Long chapterId;
    private final CodingLanguage language;
    private final String traceMode;
    private final String message;
    private final List<ExecutionFlowStep> steps;

    public GrammarTemplateExecutionFlowResponse(
        Long templateId,
        Long chapterId,
        CodingLanguage language,
        List<ExecutionFlowStep> steps
    ) {
        this.templateId = templateId;
        this.chapterId = chapterId;
        this.language = language;
        this.traceMode = "STATIC_LINE_SEQUENCE";
        this.message = "Static line-order flow with inferred variable and output states. It is not a runtime debugger trace.";
        this.steps = steps;
    }

    @Getter
    public static class ExecutionFlowStep {

        private final int stepOrder;
        private final int lineNumber;
        private final String sourceLine;
        private final String eventType;
        private final String description;
        private final VariableSnapshot activeVariable;
        private final OutputSnapshot activeOutput;
        private final List<VariableSnapshot> variables;
        private final List<OutputSnapshot> outputs;

        public ExecutionFlowStep(
            int stepOrder,
            int lineNumber,
            String sourceLine,
            String eventType,
            String description
        ) {
            this(stepOrder, lineNumber, sourceLine, eventType, description, null, null, List.of(), List.of());
        }

        public ExecutionFlowStep(
            int stepOrder,
            int lineNumber,
            String sourceLine,
            String eventType,
            String description,
            VariableSnapshot activeVariable,
            OutputSnapshot activeOutput,
            List<VariableSnapshot> variables,
            List<OutputSnapshot> outputs
        ) {
            this.stepOrder = stepOrder;
            this.lineNumber = lineNumber;
            this.sourceLine = sourceLine;
            this.eventType = eventType;
            this.description = description;
            this.activeVariable = activeVariable;
            this.activeOutput = activeOutput;
            this.variables = variables == null ? List.of() : List.copyOf(variables);
            this.outputs = outputs == null ? List.of() : List.copyOf(outputs);
        }
    }

    @Getter
    public static class VariableSnapshot {

        private final String name;
        private final String value;
        private final String expression;
        private final String dataType;
        private final String changeType;
        private final int lineNumber;
        private final int stepOrder;

        public VariableSnapshot(
            String name,
            String value,
            String expression,
            String dataType,
            String changeType,
            int lineNumber,
            int stepOrder
        ) {
            this.name = name;
            this.value = value;
            this.expression = expression;
            this.dataType = dataType;
            this.changeType = changeType;
            this.lineNumber = lineNumber;
            this.stepOrder = stepOrder;
        }
    }

    @Getter
    public static class OutputSnapshot {

        private final String value;
        private final String expression;
        private final int lineNumber;
        private final int stepOrder;

        public OutputSnapshot(
            String value,
            String expression,
            int lineNumber,
            int stepOrder
        ) {
            this.value = value;
            this.expression = expression;
            this.lineNumber = lineNumber;
            this.stepOrder = stepOrder;
        }
    }
}
