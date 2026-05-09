package com.cobip.dto.coding;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class CodingProblemDetailResponse {

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
    private final List<CodingProblemSampleTestCaseResponse> sampleTestCases;
    private final List<CodingProblemStarterCodeResponse> starterCodes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CodingProblemDetailResponse(
        CodingProblem problem,
        List<CodingProblemSampleTestCaseResponse> sampleTestCases,
        List<CodingProblemStarterCodeResponse> starterCodes
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
        this.sampleTestCases = sampleTestCases;
        this.starterCodes = starterCodes;
        this.createdAt = problem.getCreatedAt();
        this.updatedAt = problem.getUpdatedAt();
    }

    public static CodingProblemDetailResponse of(
        CodingProblem problem,
        List<CodingProblemSampleTestCaseResponse> sampleTestCases,
        List<CodingProblemStarterCodeResponse> starterCodes
    ) {
        return new CodingProblemDetailResponse(problem, sampleTestCases, starterCodes);
    }
}
