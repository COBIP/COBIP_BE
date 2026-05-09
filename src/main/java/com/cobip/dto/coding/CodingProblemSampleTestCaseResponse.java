package com.cobip.dto.coding;

import com.cobip.domain.coding.CodingProblemTestCase;

import lombok.Getter;

@Getter
public class CodingProblemSampleTestCaseResponse {

    private final Long id;
    private final String input;
    private final String expectedOutput;
    private final int orderIndex;

    private CodingProblemSampleTestCaseResponse(CodingProblemTestCase testCase) {
        this.id = testCase.getId();
        this.input = testCase.getInput();
        this.expectedOutput = testCase.getExpectedOutput();
        this.orderIndex = testCase.getOrderIndex();
    }

    public static CodingProblemSampleTestCaseResponse from(CodingProblemTestCase testCase) {
        return new CodingProblemSampleTestCaseResponse(testCase);
    }
}
