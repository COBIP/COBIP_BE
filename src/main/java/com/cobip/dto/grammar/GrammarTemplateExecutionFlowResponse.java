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
        this.message = "Static line-order flow. It is not a runtime debugger trace.";
        this.steps = steps;
    }

    @Getter
    public static class ExecutionFlowStep {

        private final int stepOrder;
        private final int lineNumber;
        private final String sourceLine;
        private final String eventType;
        private final String description;

        public ExecutionFlowStep(
            int stepOrder,
            int lineNumber,
            String sourceLine,
            String eventType,
            String description
        ) {
            this.stepOrder = stepOrder;
            this.lineNumber = lineNumber;
            this.sourceLine = sourceLine;
            this.eventType = eventType;
            this.description = description;
        }
    }
}
