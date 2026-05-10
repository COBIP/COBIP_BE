package com.cobip.web.admin;

import com.cobip.domain.template.AdminTemplateService;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminTemplateDetailResponse;
import com.cobip.dto.admin.AdminTemplateExposureUpdateRequest;
import com.cobip.dto.admin.AdminTemplateSummaryResponse;
import com.cobip.dto.template.TemplateCreateRequest;
import com.cobip.dto.template.TemplateUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/templates")
public class AdminTemplateController {

    private final AdminTemplateService adminTemplateService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminTemplateSummaryResponse>>> getTemplates(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) TemplateDifficulty difficulty,
        @RequestParam(required = false) TemplateVisibility visibility,
        @RequestParam(required = false) TemplateAccessLevel accessLevel,
        @RequestParam(required = false) Long ownerId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminTemplateService.getTemplates(keyword, category, difficulty, visibility, accessLevel, ownerId, pageable)
        ));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<AdminTemplateDetailResponse>> getTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(adminTemplateService.getTemplate(templateId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminTemplateDetailResponse>> createTemplate(
        @RequestBody @Valid TemplateCreateRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin template created.",
                adminTemplateService.createTemplate(request, adminUser)
        ));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiResponse<AdminTemplateDetailResponse>> updateTemplate(
        @PathVariable Long templateId,
        @RequestBody @Valid TemplateUpdateRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin template updated.",
                adminTemplateService.updateTemplate(templateId, request, adminUser)
        ));
    }

    @PatchMapping("/{templateId}/exposure")
    public ResponseEntity<ApiResponse<AdminTemplateDetailResponse>> updateExposure(
        @PathVariable Long templateId,
        @RequestBody @Valid AdminTemplateExposureUpdateRequest request,
        @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin template exposure updated.",
                adminTemplateService.updateExposure(templateId, request, adminUser)
        ));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
        @PathVariable Long templateId,
        @AuthenticationPrincipal User adminUser
    ) {
        adminTemplateService.deleteTemplate(templateId, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Admin template deleted.", null));
    }
}
