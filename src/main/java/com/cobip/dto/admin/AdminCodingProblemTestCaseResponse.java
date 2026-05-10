package com.cobip.dto.admin;

import com.cobip.domain.coding.CodingProblemTestCase;

import lombok.Getter;

@Getter
public class AdminCodingProblemTestCaseResponse {

    private final Long id;
    private final String input;
    private final String expectedOutput;
    private final boolean sample;
    private final int orderIndex;

    private AdminCodingProblemTestCaseResponse(CodingProblemTestCase testCase) {
        this.id = testCase.getId();
        this.input = testCase.getInput();
        this.expectedOutput = testCase.getExpectedOutput();
        this.sample = testCase.isSample();
        this.orderIndex = testCase.getOrderIndex();
    }

    public static AdminCodingProblemTestCaseResponse from(CodingProblemTestCase testCase) {
        return new AdminCodingProblemTestCaseResponse(testCase);
    }
}
