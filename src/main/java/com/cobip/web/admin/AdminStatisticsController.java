package com.cobip.web.admin;

import com.cobip.domain.admin.AdminStatisticsService;
import com.cobip.dto.admin.AdminOperationStatisticsResponse;
import com.cobip.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOperationStatisticsResponse>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getOverview()));
    }
}
