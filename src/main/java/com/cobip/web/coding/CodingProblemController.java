package com.cobip.web.coding;

import com.cobip.domain.coding.CodingProblemService;
import com.cobip.dto.coding.CodingProblemDetailResponse;
import com.cobip.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coding-problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<CodingProblemDetailResponse>> getProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.success(codingProblemService.getProblem(problemId)));
    }
}
