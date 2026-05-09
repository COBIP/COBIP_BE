package com.cobip.web.admin;

import com.cobip.domain.practice.AdminTemplatePracticeService;
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeFileRequest;
import com.cobip.dto.practice.TemplatePracticeFileResponse;
import com.cobip.dto.practice.TemplatePracticeMissionRequest;
import com.cobip.dto.practice.TemplatePracticeMissionResponse;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/templates/{templateId}/practice")
public class AdminTemplatePracticeController {

    private final AdminTemplatePracticeService adminTemplatePracticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<TemplatePracticeDetailResponse>> getPractice(@PathVariable Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(adminTemplatePracticeService.getPractice(templateId)));
    }

    @PostMapping("/files")
    public ResponseEntity<ApiResponse<TemplatePracticeFileResponse>> createFile(
        @PathVariable Long templateId,
        @RequestBody @Valid TemplatePracticeFileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminTemplatePracticeService.createFile(templateId, request)));
    }

    @PatchMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<TemplatePracticeFileResponse>> updateFile(
        @PathVariable Long templateId,
        @PathVariable Long fileId,
        @RequestBody @Valid TemplatePracticeFileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminTemplatePracticeService.updateFile(templateId, fileId, request)
        ));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
        @PathVariable Long templateId,
        @PathVariable Long fileId
    ) {
        adminTemplatePracticeService.deleteFile(templateId, fileId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/missions")
    public ResponseEntity<ApiResponse<TemplatePracticeMissionResponse>> createMission(
        @PathVariable Long templateId,
        @RequestBody @Valid TemplatePracticeMissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminTemplatePracticeService.createMission(templateId, request)));
    }

    @PatchMapping("/missions/{missionId}")
    public ResponseEntity<ApiResponse<TemplatePracticeMissionResponse>> updateMission(
        @PathVariable Long templateId,
        @PathVariable Long missionId,
        @RequestBody @Valid TemplatePracticeMissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminTemplatePracticeService.updateMission(templateId, missionId, request)
        ));
    }

    @DeleteMapping("/missions/{missionId}")
    public ResponseEntity<ApiResponse<Void>> deleteMission(
        @PathVariable Long templateId,
        @PathVariable Long missionId
    ) {
        adminTemplatePracticeService.deleteMission(templateId, missionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
