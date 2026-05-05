package com.cobip.web.admin;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateMediaType;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateDetailResponse;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplateStatusUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplateSummaryResponse;
import com.cobip.dto.grammar.GrammarTemplateUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/admin/grammar-templates")
public class AdminGrammarTemplateController {

    private final GrammarTemplateService grammarTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrammarTemplateDetailResponse>> createGrammarTemplate(
        @RequestBody @Valid GrammarTemplateCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "문법 템플릿이 등록되었습니다.",
                grammarTemplateService.createGrammarTemplate(request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GrammarTemplateSummaryResponse>>> getGrammarTemplates(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) GrammarTemplateLanguage language,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) GrammarTemplateDifficulty difficulty,
        @RequestParam(required = false) GrammarTemplateStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                grammarTemplateService.getGrammarTemplates(keyword, language, category, difficulty, status, pageable)
        ));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<GrammarTemplateDetailResponse>> getGrammarTemplate(
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarTemplateService.getGrammarTemplate(templateId)));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiResponse<GrammarTemplateDetailResponse>> updateGrammarTemplate(
        @PathVariable Long templateId,
        @RequestBody @Valid GrammarTemplateUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "문법 템플릿이 수정되었습니다.",
                grammarTemplateService.updateGrammarTemplate(templateId, request)
        ));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteGrammarTemplate(
        @PathVariable Long templateId
    ) {
        grammarTemplateService.deleteGrammarTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("문법 템플릿이 삭제되었습니다.", null));
    }

    @PatchMapping("/{templateId}/status")
    public ResponseEntity<ApiResponse<GrammarTemplateDetailResponse>> changeStatus(
        @PathVariable Long templateId,
        @RequestBody @Valid GrammarTemplateStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "문법 템플릿 공개 상태가 변경되었습니다.",
                grammarTemplateService.changeStatus(templateId, request.getStatus())
        ));
    }

    @PostMapping("/{templateId}/media")
    public ResponseEntity<ApiResponse<GrammarTemplateMediaUploadResponse>> uploadMedia(
        @PathVariable Long templateId,
        @RequestParam GrammarTemplateMediaType type,
        @RequestPart MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template media uploaded.",
                grammarTemplateService.uploadMedia(templateId, type, file)
        ));
    }
}
