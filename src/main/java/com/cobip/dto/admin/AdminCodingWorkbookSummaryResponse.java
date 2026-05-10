package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbook;
import com.cobip.domain.coding.CodingWorkbookStatus;

import lombok.Getter;

@Getter
public class AdminCodingWorkbookSummaryResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final String summary;
    private final CodingWorkbookStatus status;
    private final int displayOrder;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminCodingWorkbookSummaryResponse(CodingWorkbook workbook) {
        this.id = workbook.getId();
        this.slug = workbook.getSlug();
        this.title = workbook.getTitle();
        this.category = workbook.getCategory();
        this.difficulty = workbook.getDifficulty();
        this.summary = workbook.getSummary();
        this.status = workbook.getStatus();
        this.displayOrder = workbook.getDisplayOrder();
        this.createdAt = workbook.getCreatedAt();
        this.updatedAt = workbook.getUpdatedAt();
    }

    public static AdminCodingWorkbookSummaryResponse from(CodingWorkbook workbook) {
        return new AdminCodingWorkbookSummaryResponse(workbook);
    }
}
