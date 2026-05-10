package com.cobip.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingProblemTestCaseRequest {

    @NotNull(message = "Input is required.")
    private String input;

    @NotBlank(message = "Expected output is required.")
    private String expectedOutput;

    @NotNull(message = "Sample flag is required.")
    private Boolean sample;

    @NotNull(message = "Order index is required.")
    private Integer orderIndex;
}
