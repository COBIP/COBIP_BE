package com.cobip.web.grammar;

import java.util.List;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateExecutionService;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateCodeRunResponse;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse;
import com.cobip.dto.grammar.GrammarTemplatePublicDetailResponse;
import com.cobip.dto.grammar.GrammarTemplatePublicSummaryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/grammar-templates")
public class GrammarTemplateController {

    private final GrammarTemplateService grammarTemplateService;
    private final GrammarTemplateExecutionService grammarTemplateExecutionService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories(
        @RequestParam(required = false) GrammarTemplateLanguage language
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarTemplateService.getPublishedCategories(language)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GrammarTemplatePublicSummaryResponse>>> getGrammarTemplates(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) GrammarTemplateLanguage language,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) GrammarTemplateDifficulty difficulty,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                grammarTemplateService.getPublishedGrammarTemplates(keyword, language, category, difficulty, pageable)
        ));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<GrammarTemplatePublicDetailResponse>> getGrammarTemplate(
        @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(grammarTemplateService.getPublishedGrammarTemplate(templateId)));
    }

    @PostMapping("/{templateId}/chapters/{chapterId}/run")
    public ResponseEntity<ApiResponse<GrammarTemplateCodeRunResponse>> runChapter(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @RequestBody @Valid GrammarTemplateCodeRunRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                grammarTemplateExecutionService.runChapter(templateId, chapterId, request)
        ));
    }

    @PostMapping("/{templateId}/chapters/{chapterId}/execution-flow")
    public ResponseEntity<ApiResponse<GrammarTemplateExecutionFlowResponse>> getExecutionFlow(
        @PathVariable Long templateId,
        @PathVariable Long chapterId,
        @RequestBody @Valid GrammarTemplateExecutionFlowRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                grammarTemplateExecutionService.getExecutionFlow(templateId, chapterId, request)
        ));
    }
}
