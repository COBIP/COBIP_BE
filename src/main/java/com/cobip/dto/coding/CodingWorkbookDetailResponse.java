package com.cobip.dto.coding;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingWorkbook;

import lombok.Getter;

@Getter
public class CodingWorkbookDetailResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final String summary;
    private final String description;
    private final List<CodingWorkbookProblemSummaryResponse> problems;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CodingWorkbookDetailResponse(CodingWorkbook workbook, List<CodingProblem> problems, Set<Long> solvedProblemIds) {
        this.id = workbook.getId();
        this.slug = workbook.getSlug();
        this.title = workbook.getTitle();
        this.category = workbook.getCategory();
        this.difficulty = workbook.getDifficulty();
        this.summary = workbook.getSummary();
        this.description = workbook.getDescription();
        this.problems = problems.stream()
                .map(problem -> CodingWorkbookProblemSummaryResponse.from(problem, solvedProblemIds.contains(problem.getId())))
                .toList();
        this.createdAt = workbook.getCreatedAt();
        this.updatedAt = workbook.getUpdatedAt();
    }

    public static CodingWorkbookDetailResponse of(
        CodingWorkbook workbook,
        List<CodingProblem> problems,
        Set<Long> solvedProblemIds
    ) {
        return new CodingWorkbookDetailResponse(workbook, problems, solvedProblemIds);
    }
}
