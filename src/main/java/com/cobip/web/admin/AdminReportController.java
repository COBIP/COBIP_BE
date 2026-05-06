package com.cobip.web.admin;

import com.cobip.domain.report.AdminReportService;
import com.cobip.domain.report.ReportStatus;
import com.cobip.domain.report.ReportTargetType;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminReportDetailResponse;
import com.cobip.dto.admin.AdminReportStatusUpdateRequest;
import com.cobip.dto.admin.AdminReportSummaryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReportSummaryResponse>>> getReports(
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) ReportTargetType targetType,
        @RequestParam(required = false) Long reporterId,
        @RequestParam(required = false) Long targetId,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminReportService.getReports(
                status,
                targetType,
                reporterId,
                targetId,
                keyword,
                pageable
        )));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.success(adminReportService.getReport(reportId)));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> updateStatus(
        @PathVariable Long reportId,
        @RequestBody @Valid AdminReportStatusUpdateRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Report status updated.",
                adminReportService.updateStatus(reportId, request, adminUser)
        ));
    }
}
