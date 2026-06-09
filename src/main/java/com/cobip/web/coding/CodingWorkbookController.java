package com.cobip.web.coding;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookService;
import com.cobip.domain.user.User;
import com.cobip.dto.coding.CodingWorkbookDetailResponse;
import com.cobip.dto.coding.CodingWorkbookSummaryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coding-workbooks")
public class CodingWorkbookController {

    private final CodingWorkbookService codingWorkbookService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CodingWorkbookSummaryResponse>>> getWorkbooks(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) CodingDifficulty difficulty,
        @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                codingWorkbookService.getWorkbooks(keyword, category, difficulty, pageable)
        ));
    }

    @GetMapping("/{workbookId}")
    public ResponseEntity<ApiResponse<CodingWorkbookDetailResponse>> getWorkbook(
        @AuthenticationPrincipal User user,
        @PathVariable Long workbookId
    ) {
        return ResponseEntity.ok(ApiResponse.success(codingWorkbookService.getWorkbook(user, workbookId)));
    }
}
