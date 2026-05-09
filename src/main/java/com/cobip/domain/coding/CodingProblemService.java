package com.cobip.domain.coding;

import com.cobip.dto.coding.CodingProblemDetailResponse;
import com.cobip.dto.coding.CodingProblemSampleTestCaseResponse;
import com.cobip.dto.coding.CodingProblemStarterCodeResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodingProblemService {

    private final CodingProblemRepository codingProblemRepository;
    private final CodingProblemTestCaseRepository codingProblemTestCaseRepository;
    private final CodingProblemStarterCodeRepository codingProblemStarterCodeRepository;

    @Transactional(readOnly = true)
    public CodingProblemDetailResponse getProblem(Long problemId) {
        CodingProblem problem = codingProblemRepository
                .findByIdAndStatusAndDeletedAtIsNull(problemId, CodingProblemStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_PROBLEM_NOT_FOUND));
        var sampleTestCases = codingProblemTestCaseRepository
                .findByProblemIdAndSampleOrderByOrderIndexAsc(problem.getId(), true)
                .stream()
                .map(CodingProblemSampleTestCaseResponse::from)
                .toList();
        var starterCodes = codingProblemStarterCodeRepository
                .findByProblemIdOrderByLanguageAsc(problem.getId())
                .stream()
                .map(CodingProblemStarterCodeResponse::from)
                .toList();

        return CodingProblemDetailResponse.of(problem, sampleTestCases, starterCodes);
    }
}
