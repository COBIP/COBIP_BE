package com.cobip.dto.report;

import com.cobip.domain.report.ReportTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReportCreateRequest {

    @NotNull
    private ReportTargetType targetType;

    @NotNull
    @Positive
    private Long targetId;

    @NotBlank
    @Size(max = 120)
    private String reason;

    @Size(max = 2000)
    private String description;
}
