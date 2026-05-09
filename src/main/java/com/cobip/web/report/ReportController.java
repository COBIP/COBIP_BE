package com.cobip.web.report;

import com.cobip.domain.report.ReportService;
import com.cobip.domain.user.User;
import com.cobip.dto.report.ReportCreateRequest;
import com.cobip.dto.report.ReportResponse;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid ReportCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다.", reportService.createReport(user, request)));
    }
}
