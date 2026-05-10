package com.cobip.web.admin;

import java.util.List;

import com.cobip.domain.coding.AdminCodingWorkbookService;
import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookStatus;
import com.cobip.dto.admin.AdminCodingProblemCreateRequest;
import com.cobip.dto.admin.AdminCodingProblemDetailResponse;
import com.cobip.dto.admin.AdminCodingProblemSummaryResponse;
import com.cobip.dto.admin.AdminCodingProblemUpdateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookCreateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookDetailResponse;
import com.cobip.dto.admin.AdminCodingWorkbookSummaryResponse;
import com.cobip.dto.admin.AdminCodingWorkbookUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coding-workbooks")
public class AdminCodingWorkbookController {

    private final AdminCodingWorkbookService adminCodingWorkbookService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminCodingWorkbookSummaryResponse>>> getWorkbooks(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) CodingDifficulty difficulty,
        @RequestParam(required = false) CodingWorkbookStatus status,
        @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCodingWorkbookService.getWorkbooks(keyword, category, difficulty, status, pageable)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminCodingWorkbookDetailResponse>> createWorkbook(
        @RequestBody @Valid AdminCodingWorkbookCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin coding workbook created.",
                adminCodingWorkbookService.createWorkbook(request)
        ));
    }

    @GetMapping("/{workbookId}")
    public ResponseEntity<ApiResponse<AdminCodingWorkbookDetailResponse>> getWorkbook(@PathVariable Long workbookId) {
        return ResponseEntity.ok(ApiResponse.success(adminCodingWorkbookService.getWorkbook(workbookId)));
    }

    @PatchMapping("/{workbookId}")
    public ResponseEntity<ApiResponse<AdminCodingWorkbookDetailResponse>> updateWorkbook(
        @PathVariable Long workbookId,
        @RequestBody @Valid AdminCodingWorkbookUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin coding workbook updated.",
                adminCodingWorkbookService.updateWorkbook(workbookId, request)
        ));
    }

    @DeleteMapping("/{workbookId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkbook(@PathVariable Long workbookId) {
        adminCodingWorkbookService.deleteWorkbook(workbookId);
        return ResponseEntity.ok(ApiResponse.success("Admin coding workbook deleted.", null));
    }

    @GetMapping("/{workbookId}/problems")
    public ResponseEntity<ApiResponse<List<AdminCodingProblemSummaryResponse>>> getProblems(
        @PathVariable Long workbookId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminCodingWorkbookService.getProblems(workbookId)));
    }

    @PostMapping("/{workbookId}/problems")
    public ResponseEntity<ApiResponse<AdminCodingProblemDetailResponse>> createProblem(
        @PathVariable Long workbookId,
        @RequestBody @Valid AdminCodingProblemCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin coding problem created.",
                adminCodingWorkbookService.createProblem(workbookId, request)
        ));
    }

    @GetMapping("/{workbookId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<AdminCodingProblemDetailResponse>> getProblem(
        @PathVariable Long workbookId,
        @PathVariable Long problemId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminCodingWorkbookService.getProblem(workbookId, problemId)));
    }

    @PatchMapping("/{workbookId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<AdminCodingProblemDetailResponse>> updateProblem(
        @PathVariable Long workbookId,
        @PathVariable Long problemId,
        @RequestBody @Valid AdminCodingProblemUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin coding problem updated.",
                adminCodingWorkbookService.updateProblem(workbookId, problemId, request)
        ));
    }

    @DeleteMapping("/{workbookId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(
        @PathVariable Long workbookId,
        @PathVariable Long problemId
    ) {
        adminCodingWorkbookService.deleteProblem(workbookId, problemId);
        return ResponseEntity.ok(ApiResponse.success("Admin coding problem deleted.", null));
    }
}
