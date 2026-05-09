package com.cobip.dto.coding;

import java.time.LocalDateTime;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbook;

import lombok.Getter;

@Getter
public class CodingWorkbookSummaryResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final String category;
    private final CodingDifficulty difficulty;
    private final String summary;
    private final int displayOrder;
    private final LocalDateTime createdAt;

    private CodingWorkbookSummaryResponse(CodingWorkbook workbook) {
        this.id = workbook.getId();
        this.slug = workbook.getSlug();
        this.title = workbook.getTitle();
        this.category = workbook.getCategory();
        this.difficulty = workbook.getDifficulty();
        this.summary = workbook.getSummary();
        this.displayOrder = workbook.getDisplayOrder();
        this.createdAt = workbook.getCreatedAt();
    }

    public static CodingWorkbookSummaryResponse from(CodingWorkbook workbook) {
        return new CodingWorkbookSummaryResponse(workbook);
    }
}
