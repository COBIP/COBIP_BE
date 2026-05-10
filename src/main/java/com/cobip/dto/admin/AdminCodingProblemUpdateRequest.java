package com.cobip.dto.admin;

import java.util.List;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingProblemStatus;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingProblemUpdateRequest {

    @Size(max = 120, message = "Title must be 120 characters or less.")
    private String title;

    @Size(max = 80, message = "Category must be 80 characters or less.")
    private String category;

    private CodingDifficulty difficulty;

    private JsonNode contentJson;

    private JsonNode explanationJson;

    private Integer orderIndex;

    private Integer timeLimitMillis;

    private Integer memoryLimitMb;

    private CodingProblemStatus status;

    @Valid
    private List<AdminCodingProblemTestCaseRequest> testCases;

    @Valid
    private List<AdminCodingProblemStarterCodeRequest> starterCodes;
}
