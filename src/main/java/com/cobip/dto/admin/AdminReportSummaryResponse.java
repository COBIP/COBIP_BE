package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.report.Report;
import com.cobip.domain.report.ReportStatus;
import com.cobip.domain.report.ReportTargetType;
import com.cobip.domain.user.User;

import lombok.Getter;

@Getter
public class AdminReportSummaryResponse {

    private final Long id;
    private final Long reporterId;
    private final String reporterEmail;
    private final String reporterNickname;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final String reason;
    private final ReportStatus status;
    private final Long processedById;
    private final String processedByNickname;
    private final LocalDateTime processedAt;
    private final LocalDateTime createdAt;

    private AdminReportSummaryResponse(Report report) {
        User reporter = report.getReporter();
        User processedBy = report.getProcessedBy();

        this.id = report.getId();
        this.reporterId = reporter.getId();
        this.reporterEmail = reporter.getEmail();
        this.reporterNickname = reporter.getNickname();
        this.targetType = report.getTargetType();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.processedById = processedBy == null ? null : processedBy.getId();
        this.processedByNickname = processedBy == null ? null : processedBy.getNickname();
        this.processedAt = report.getProcessedAt();
        this.createdAt = report.getCreatedAt();
    }

    public static AdminReportSummaryResponse from(Report report) {
        return new AdminReportSummaryResponse(report);
    }
}
