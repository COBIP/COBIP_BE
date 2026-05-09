package com.cobip.web.coding;

import com.cobip.domain.coding.CodingProblemService;
import com.cobip.domain.coding.CodingSubmissionService;
import com.cobip.domain.user.User;
import com.cobip.dto.coding.CodingCodeRunRequest;
import com.cobip.dto.coding.CodingCodeRunResponse;
import com.cobip.dto.coding.CodingProblemDetailResponse;
import com.cobip.dto.coding.CodingSubmissionRequest;
import com.cobip.dto.coding.CodingSubmissionResponse;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coding-problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;
    private final CodingSubmissionService codingSubmissionService;

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<CodingProblemDetailResponse>> getProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.success(codingProblemService.getProblem(problemId)));
    }

    @PostMapping("/{problemId}/run")
    public ResponseEntity<ApiResponse<CodingCodeRunResponse>> runProblem(
        @PathVariable Long problemId,
        @RequestBody @Valid CodingCodeRunRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(codingSubmissionService.runProblem(problemId, request)));
    }

    @PostMapping("/{problemId}/submissions")
    public ResponseEntity<ApiResponse<CodingSubmissionResponse>> submitProblem(
        @AuthenticationPrincipal User user,
        @PathVariable Long problemId,
        @RequestBody @Valid CodingSubmissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(codingSubmissionService.submitProblem(user, problemId, request)));
    }
}
