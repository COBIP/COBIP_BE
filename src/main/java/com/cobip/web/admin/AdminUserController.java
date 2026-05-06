package com.cobip.web.admin;

import com.cobip.domain.user.AdminUserService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.admin.AdminUserDetailResponse;
import com.cobip.dto.admin.AdminUserStatusUpdateRequest;
import com.cobip.dto.admin.AdminUserSummaryResponse;
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
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserSummaryResponse>>> getUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) Boolean emailVerified,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminUserService.getUsers(keyword, role, status, emailVerified, pageable)
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUser(userId)));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> changeStatus(
        @PathVariable Long userId,
        @RequestBody @Valid AdminUserStatusUpdateRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin user status updated.",
                adminUserService.changeStatus(userId, request, adminUser)
        ));
    }
}
