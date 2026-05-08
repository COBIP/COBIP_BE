package com.cobip.dto.coding;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingProblemDifficulty;

import lombok.Getter;

@Getter
public class CodingProblemDetailResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final CodingProblemDifficulty difficulty;
    private final String description;
    private final String inputDescription;
    private final String outputDescription;
    private final String sampleInput;
    private final String sampleOutput;
    private final int timeLimitMillis;
    private final int memoryLimitMb;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CodingProblemDetailResponse(CodingProblem problem) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.description = problem.getDescription();
        this.inputDescription = problem.getInputDescription();
        this.outputDescription = problem.getOutputDescription();
        this.sampleInput = problem.getSampleInput();
        this.sampleOutput = problem.getSampleOutput();
        this.timeLimitMillis = problem.getTimeLimitMillis();
        this.memoryLimitMb = problem.getMemoryLimitMb();
        this.createdAt = problem.getCreatedAt();
        this.updatedAt = problem.getUpdatedAt();
    }

    public static CodingProblemDetailResponse from(CodingProblem problem) {
        return new CodingProblemDetailResponse(problem);
    }
}
