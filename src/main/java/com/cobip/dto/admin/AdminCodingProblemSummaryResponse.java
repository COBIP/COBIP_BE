package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingProblemStatus;

import lombok.Getter;

@Getter
public class AdminCodingProblemSummaryResponse {

    private final Long id;
    private final Long workbookId;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final int orderIndex;
    private final int timeLimitMillis;
    private final int memoryLimitMb;
    private final CodingProblemStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminCodingProblemSummaryResponse(CodingProblem problem) {
        this.id = problem.getId();
        this.workbookId = problem.getWorkbook().getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.orderIndex = problem.getOrderIndex();
        this.timeLimitMillis = problem.getTimeLimitMillis();
        this.memoryLimitMb = problem.getMemoryLimitMb();
        this.status = problem.getStatus();
        this.createdAt = problem.getCreatedAt();
        this.updatedAt = problem.getUpdatedAt();
    }

    public static AdminCodingProblemSummaryResponse from(CodingProblem problem) {
        return new AdminCodingProblemSummaryResponse(problem);
    }
}
