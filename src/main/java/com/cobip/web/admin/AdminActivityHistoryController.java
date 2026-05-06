package com.cobip.web.admin;

import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.activity.AdminActivityHistoryService;
import com.cobip.dto.admin.AdminActivityHistoryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/activity-histories")
public class AdminActivityHistoryController {

    private final AdminActivityHistoryService adminActivityHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminActivityHistoryResponse>>> getActivityHistories(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) ActivityType type,
        @RequestParam(required = false) String targetType,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminActivityHistoryService.getActivityHistories(userId, type, targetType, pageable)
        ));
    }
}
