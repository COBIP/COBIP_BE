package com.cobip.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingProblemStarterCode;
import com.cobip.domain.coding.CodingProblemStatus;
import com.cobip.domain.coding.CodingProblemTestCase;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class AdminCodingProblemDetailResponse {

    private final Long id;
    private final Long workbookId;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final JsonNode contentJson;
    private final JsonNode explanationJson;
    private final int orderIndex;
    private final int timeLimitMillis;
    private final int memoryLimitMb;
    private final CodingProblemStatus status;
    private final List<AdminCodingProblemTestCaseResponse> testCases;
    private final List<AdminCodingProblemStarterCodeResponse> starterCodes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminCodingProblemDetailResponse(
        CodingProblem problem,
        List<CodingProblemTestCase> testCases,
        List<CodingProblemStarterCode> starterCodes
    ) {
        this.id = problem.getId();
        this.workbookId = problem.getWorkbook().getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.contentJson = problem.getContentJson();
        this.explanationJson = problem.getExplanationJson();
        this.orderIndex = problem.getOrderIndex();
        this.timeLimitMillis = problem.getTimeLimitMillis();
        this.memoryLimitMb = problem.getMemoryLimitMb();
        this.status = problem.getStatus();
        this.testCases = testCases.stream()
                .map(AdminCodingProblemTestCaseResponse::from)
                .toList();
        this.starterCodes = starterCodes.stream()
                .map(AdminCodingProblemStarterCodeResponse::from)
                .toList();
        this.createdAt = problem.getCreatedAt();
        this.updatedAt = problem.getUpdatedAt();
    }

    public static AdminCodingProblemDetailResponse of(
        CodingProblem problem,
        List<CodingProblemTestCase> testCases,
        List<CodingProblemStarterCode> starterCodes
    ) {
        return new AdminCodingProblemDetailResponse(problem, testCases, starterCodes);
    }
}
