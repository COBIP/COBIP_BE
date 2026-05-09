package com.cobip.web.practice;

import com.cobip.domain.practice.TemplatePracticeService;
import com.cobip.domain.user.User;
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeMissionProgressUpdateRequest;
import com.cobip.dto.practice.TemplatePracticeProgressResponse;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates/{templateId}/practice")
public class TemplatePracticeController {

    private final TemplatePracticeService templatePracticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<TemplatePracticeDetailResponse>> getPractice(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(templatePracticeService.getPractice(user, templateId)));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TemplatePracticeProgressResponse>> startPractice(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(templatePracticeService.startPractice(user, templateId)));
    }

    @PatchMapping("/missions/{missionId}/progress")
    public ResponseEntity<ApiResponse<TemplatePracticeProgressResponse>> updateMissionProgress(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId,
        @PathVariable Long missionId,
        @RequestBody @Valid TemplatePracticeMissionProgressUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                templatePracticeService.updateMissionProgress(user, templateId, missionId, request)
        ));
    }
}
