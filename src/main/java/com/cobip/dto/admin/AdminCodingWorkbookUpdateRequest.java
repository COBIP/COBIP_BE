package com.cobip.dto.admin;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookStatus;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingWorkbookUpdateRequest {

    @Size(max = 120, message = "Slug must be 120 characters or less.")
    private String slug;

    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    private CodingDifficulty difficulty;

    @Size(max = 500, message = "Summary must be 500 characters or less.")
    private String summary;

    private String description;

    private CodingWorkbookStatus status;

    private Integer displayOrder;
}
