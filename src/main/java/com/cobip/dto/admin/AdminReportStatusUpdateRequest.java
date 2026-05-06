package com.cobip.dto.admin;

import com.cobip.domain.report.ReportStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReportStatusUpdateRequest {

    @NotNull(message = "status is required.")
    private ReportStatus status;

    @Size(max = 2000, message = "adminMemo must be 2000 characters or fewer.")
    private String adminMemo;
}
