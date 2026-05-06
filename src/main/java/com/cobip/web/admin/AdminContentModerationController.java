package com.cobip.web.admin;

import com.cobip.domain.moderation.AdminContentModerationService;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminContentModerationRequest;
import com.cobip.dto.admin.AdminContentModerationResponse;
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
@RequestMapping("/api/v1/admin/content-moderations")
public class AdminContentModerationController {

    private final AdminContentModerationService adminContentModerationService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminContentModerationResponse>> moderate(
        @RequestBody @Valid AdminContentModerationRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin content moderation completed.",
                adminContentModerationService.moderate(request, adminUser)
        ));
    }
}
