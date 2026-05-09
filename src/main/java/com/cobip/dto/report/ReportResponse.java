package com.cobip.dto.report;

import java.time.LocalDateTime;

import com.cobip.domain.report.Report;
import com.cobip.domain.report.ReportStatus;
import com.cobip.domain.report.ReportTargetType;

import lombok.Getter;

@Getter
public class ReportResponse {

    private final Long id;
    private final Long reporterId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final String reason;
    private final String description;
    private final ReportStatus status;
    private final LocalDateTime createdAt;

    private ReportResponse(Report report) {
        this.id = report.getId();
        this.reporterId = report.getReporter().getId();
        this.targetType = report.getTargetType();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.description = report.getDescription();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
    }

    public static ReportResponse from(Report report) {
        return new ReportResponse(report);
    }
}
