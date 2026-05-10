package com.cobip.dto.admin;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingWorkbookCreateRequest {

    @NotBlank(message = "Slug is required.")
    @Size(max = 120, message = "Slug must be 120 characters or less.")
    private String slug;

    @NotBlank(message = "Title is required.")
    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @NotBlank(message = "Category is required.")
    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    @NotNull(message = "Difficulty is required.")
    private CodingDifficulty difficulty;

    @NotBlank(message = "Summary is required.")
    @Size(max = 500, message = "Summary must be 500 characters or less.")
    private String summary;

    private String description;

    private CodingWorkbookStatus status = CodingWorkbookStatus.DRAFT;

    @NotNull(message = "Display order is required.")
    private Integer displayOrder;
}
