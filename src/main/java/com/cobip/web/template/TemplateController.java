package com.cobip.web.template;

import com.cobip.domain.certificate.CertificateService;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateService;
import com.cobip.domain.user.User;
import com.cobip.dto.mypage.CertificateResponse;
import com.cobip.dto.template.TemplateCreateRequest;
import com.cobip.dto.template.TemplateDetailResponse;
import com.cobip.dto.template.TemplateFileUploadResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.template.TemplateUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;
    private final CertificateService certificateService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getTemplates(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) TemplateDifficulty difficulty,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getTemplates(keyword, category, difficulty, pageable)));
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getRecommendedTemplates(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getRecommendedTemplates(user, pageable)));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> getTemplate(
        @PathVariable Long templateId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getTemplate(templateId, user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> createTemplate(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid TemplateCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("템플릿이 등록되었습니다.", templateService.createTemplate(user, request)));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> updateTemplate(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId,
        @RequestBody @Valid TemplateUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("템플릿이 수정되었습니다.", templateService.updateTemplate(user, templateId, request)));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        templateService.deleteTemplate(user, templateId);
        return ResponseEntity.ok(ApiResponse.success("템플릿이 삭제되었습니다.", null));
    }

    @PostMapping("/{templateId}/file")
    public ResponseEntity<ApiResponse<TemplateFileUploadResponse>> uploadFile(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId,
        @RequestPart MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success("템플릿 파일이 업로드되었습니다.", templateService.uploadFile(user, templateId, file)));
    }

    @PostMapping("/{templateId}/thumbnail")
    public ResponseEntity<ApiResponse<TemplateFileUploadResponse>> uploadThumbnail(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId,
        @RequestPart MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success("템플릿 썸네일이 업로드되었습니다.", templateService.uploadThumbnail(user, templateId, file)));
    }

    @PostMapping("/{templateId}/favorite")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        templateService.addFavorite(user, templateId);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 찜했습니다.", null));
    }

    @DeleteMapping("/{templateId}/favorite")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        templateService.removeFavorite(user, templateId);
        return ResponseEntity.ok(ApiResponse.success("템플릿 찜을 취소했습니다.", null));
    }

    @PostMapping("/{templateId}/certificates")
    public ResponseEntity<ApiResponse<CertificateResponse>> issueCertificate(
        @AuthenticationPrincipal User user,
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success("수료증이 발급되었습니다.", certificateService.issueCertificate(user, templateId)));
    }
}
