package com.cobip.dto.coding;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingProblemDifficulty;

import lombok.Getter;

@Getter
public class CodingProblemSummaryResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final CodingProblemDifficulty difficulty;
    private final int timeLimitMillis;
    private final int memoryLimitMb;
    private final LocalDateTime createdAt;

    private CodingProblemSummaryResponse(CodingProblem problem) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.timeLimitMillis = problem.getTimeLimitMillis();
        this.memoryLimitMb = problem.getMemoryLimitMb();
        this.createdAt = problem.getCreatedAt();
    }

    public static CodingProblemSummaryResponse from(CodingProblem problem) {
        return new CodingProblemSummaryResponse(problem);
    }
}
