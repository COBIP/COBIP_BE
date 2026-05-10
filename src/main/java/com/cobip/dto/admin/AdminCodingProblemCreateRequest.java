package com.cobip.dto.admin;

import java.util.List;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblemStatus;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingProblemCreateRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @NotBlank(message = "Category is required.")
    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    @NotNull(message = "Difficulty is required.")
    private CodingDifficulty difficulty;

    @NotNull(message = "Content JSON is required.")
    private JsonNode contentJson;

    private JsonNode explanationJson;

    @NotNull(message = "Order index is required.")
    private Integer orderIndex;

    @NotNull(message = "Time limit is required.")
    private Integer timeLimitMillis;

    @NotNull(message = "Memory limit is required.")
    private Integer memoryLimitMb;

    private CodingProblemStatus status = CodingProblemStatus.DRAFT;

    @Valid
    private List<AdminCodingProblemTestCaseRequest> testCases = List.of();

    @Valid
    private List<AdminCodingProblemStarterCodeRequest> starterCodes = List.of();
}
