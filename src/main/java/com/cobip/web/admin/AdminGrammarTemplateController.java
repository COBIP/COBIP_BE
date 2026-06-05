package com.cobip.web.admin;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateMediaType;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.dto.grammar.GrammarTemplateChapterCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateChapterResponse;
import com.cobip.dto.grammar.GrammarTemplateChapterUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateDetailResponse;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplateMissionRequest;
import com.cobip.dto.grammar.GrammarTemplateMissionResponse;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileRequest;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileResponse;
import com.cobip.dto.grammar.GrammarTemplateStatusUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplateSummaryResponse;
import com.cobip.dto.grammar.GrammarTemplateUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import java.util.List;

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

    @GetMapping("/{templateId}/chapters")
    public ResponseEntity<ApiResponse<List<GrammarTemplateChapterResponse>>> getChapters(
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarTemplateService.getChapters(templateId)));
    }

    @PostMapping("/{templateId}/chapters")
    public ResponseEntity<ApiResponse<GrammarTemplateChapterResponse>> createChapter(
        @PathVariable Long templateId,
        @RequestBody @Valid GrammarTemplateChapterCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template chapter created.",
                grammarTemplateService.createChapter(templateId, request)
        ));
    }

    @PatchMapping("/{templateId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<GrammarTemplateChapterResponse>> updateChapter(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @RequestBody @Valid GrammarTemplateChapterUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template chapter updated.",
                grammarTemplateService.updateChapter(templateId, chapterId, request)
        ));
    }

    @DeleteMapping("/{templateId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
        @PathVariable Long templateId,
        @PathVariable Long chapterId
    ) {
        grammarTemplateService.deleteChapter(templateId, chapterId);
        return ResponseEntity.ok(ApiResponse.success("Grammar template chapter deleted.", null));
    }

    @GetMapping("/{templateId}/chapters/{chapterId}/practice-files")
    public ResponseEntity<ApiResponse<List<GrammarTemplatePracticeFileResponse>>> getPracticeFiles(
        @PathVariable Long templateId,
        @PathVariable Long chapterId
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarTemplateService.getPracticeFiles(templateId, chapterId)));
    }

    @PostMapping("/{templateId}/chapters/{chapterId}/practice-files")
    public ResponseEntity<ApiResponse<GrammarTemplatePracticeFileResponse>> createPracticeFile(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @RequestBody @Valid GrammarTemplatePracticeFileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template practice file created.",
                grammarTemplateService.createPracticeFile(templateId, chapterId, request)
        ));
    }

    @PatchMapping("/{templateId}/chapters/{chapterId}/practice-files/{fileId}")
    public ResponseEntity<ApiResponse<GrammarTemplatePracticeFileResponse>> updatePracticeFile(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @PathVariable Long fileId,
        @RequestBody @Valid GrammarTemplatePracticeFileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template practice file updated.",
                grammarTemplateService.updatePracticeFile(templateId, chapterId, fileId, request)
        ));
    }

    @DeleteMapping("/{templateId}/chapters/{chapterId}/practice-files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deletePracticeFile(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @PathVariable Long fileId
    ) {
        grammarTemplateService.deletePracticeFile(templateId, chapterId, fileId);
        return ResponseEntity.ok(ApiResponse.success("Grammar template practice file deleted.", null));
    }

    @GetMapping("/{templateId}/chapters/{chapterId}/missions")
    public ResponseEntity<ApiResponse<List<GrammarTemplateMissionResponse>>> getChapterMissions(
        @PathVariable Long templateId,
        @PathVariable Long chapterId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                grammarTemplateService.getChapterMissions(templateId, chapterId)
        ));
    }

    @PostMapping("/{templateId}/chapters/{chapterId}/missions")
    public ResponseEntity<ApiResponse<GrammarTemplateMissionResponse>> createChapterMission(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @RequestBody @Valid GrammarTemplateMissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template chapter mission created.",
                grammarTemplateService.createChapterMission(templateId, chapterId, request)
        ));
    }

    @PatchMapping("/{templateId}/chapters/{chapterId}/missions/{missionId}")
    public ResponseEntity<ApiResponse<GrammarTemplateMissionResponse>> updateChapterMission(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @PathVariable Long missionId,
        @RequestBody @Valid GrammarTemplateMissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Grammar template chapter mission updated.",
                grammarTemplateService.updateChapterMission(templateId, chapterId, missionId, request)
        ));
    }

    @DeleteMapping("/{templateId}/chapters/{chapterId}/missions/{missionId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapterMission(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @PathVariable Long missionId
    ) {
        grammarTemplateService.deleteChapterMission(templateId, chapterId, missionId);
        return ResponseEntity.ok(ApiResponse.success("Grammar template chapter mission deleted.", null));
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
