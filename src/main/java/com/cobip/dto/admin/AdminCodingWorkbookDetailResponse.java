package com.cobip.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblem;
import com.cobip.domain.coding.CodingWorkbook;
import com.cobip.domain.coding.CodingWorkbookStatus;

import lombok.Getter;

@Getter
public class AdminCodingWorkbookDetailResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final String summary;
    private final String description;
    private final CodingWorkbookStatus status;
    private final int displayOrder;
    private final List<AdminCodingProblemSummaryResponse> problems;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminCodingWorkbookDetailResponse(CodingWorkbook workbook, List<CodingProblem> problems) {
        this.id = workbook.getId();
        this.slug = workbook.getSlug();
        this.title = workbook.getTitle();
        this.category = workbook.getCategory();
        this.difficulty = workbook.getDifficulty();
        this.summary = workbook.getSummary();
        this.description = workbook.getDescription();
        this.status = workbook.getStatus();
        this.displayOrder = workbook.getDisplayOrder();
        this.problems = problems.stream()
                .map(AdminCodingProblemSummaryResponse::from)
                .toList();
        this.createdAt = workbook.getCreatedAt();
        this.updatedAt = workbook.getUpdatedAt();
    }

    public static AdminCodingWorkbookDetailResponse of(CodingWorkbook workbook, List<CodingProblem> problems) {
        return new AdminCodingWorkbookDetailResponse(workbook, problems);
    }
}
