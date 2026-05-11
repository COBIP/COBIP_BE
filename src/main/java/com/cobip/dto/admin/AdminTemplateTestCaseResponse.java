package com.cobip.dto.admin;

import com.cobip.domain.template.TemplateTestCase;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class AdminTemplateTestCaseResponse {

    private final Long id;
    private final String input;

    @JsonProperty("expected_output")
    private final String expectedOutput;

    private final String description;
    private final int orderIndex;

    private AdminTemplateTestCaseResponse(TemplateTestCase testCase) {
        this.id = testCase.getId();
        this.input = testCase.getInput();
        this.expectedOutput = testCase.getExpectedOutput();
        this.description = testCase.getDescription();
        this.orderIndex = testCase.getOrderIndex();
    }

    public static AdminTemplateTestCaseResponse from(TemplateTestCase testCase) {
        return new AdminTemplateTestCaseResponse(testCase);
    }
}
