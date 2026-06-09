package com.cobip.dto.coding;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;

import lombok.Getter;

@Getter
public class CodingWorkbookProblemSummaryResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final int orderIndex;
    private final boolean solved;

    private CodingWorkbookProblemSummaryResponse(CodingProblem problem, boolean solved) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.category = problem.getCategory();
        this.difficulty = problem.getDifficulty();
        this.orderIndex = problem.getOrderIndex();
        this.solved = solved;
    }

    public static CodingWorkbookProblemSummaryResponse from(CodingProblem problem, boolean solved) {
        return new CodingWorkbookProblemSummaryResponse(problem, solved);
    }
}
