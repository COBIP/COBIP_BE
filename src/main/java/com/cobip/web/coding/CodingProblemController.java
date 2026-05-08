package com.cobip.web.coding;

import com.cobip.domain.coding.CodingProblemDifficulty;
import com.cobip.domain.coding.CodingProblemService;
import com.cobip.dto.coding.CodingProblemSummaryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coding-problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CodingProblemSummaryResponse>>> getProblems(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) CodingProblemDifficulty difficulty,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                codingProblemService.getProblems(keyword, category, difficulty, pageable)
        ));
    }
}
