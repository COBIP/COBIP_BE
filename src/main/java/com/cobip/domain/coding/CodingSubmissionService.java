package com.cobip.domain.coding;

import java.util.List;

import com.cobip.domain.user.User;
import com.cobip.dto.coding.CodingCodeRunRequest;
import com.cobip.dto.coding.CodingCodeRunResponse;
import com.cobip.dto.coding.CodingSubmissionRequest;
import com.cobip.dto.coding.CodingSubmissionResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodingSubmissionService {

    private final CodingProblemRepository codingProblemRepository;
    private final CodingProblemTestCaseRepository codingProblemTestCaseRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final CodeExecutionClient codeExecutionClient;

    @Transactional(readOnly = true)
    public CodingCodeRunResponse runProblem(Long problemId, CodingCodeRunRequest request) {
        CodingProblem problem = getPublishedProblem(problemId);
        CodeExecutionResult result = codeExecutionClient.execute(
                request.getLanguage(),
                request.getSourceCode(),
                normalizeInput(request.getInput()),
                null,
                problem.getTimeLimitMillis(),
                problem.getMemoryLimitMb()
        );

        return CodingCodeRunResponse.from(result);
    }

    @Transactional
    public CodingSubmissionResponse submitProblem(User user, Long problemId, CodingSubmissionRequest request) {
        CodingProblem problem = getPublishedProblem(problemId);
        List<CodingProblemTestCase> testCases = codingProblemTestCaseRepository
                .findByProblemIdOrderByOrderIndexAsc(problem.getId());

        if (testCases.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        int passedCount = 0;
        CodingSubmissionStatus finalStatus = CodingSubmissionStatus.ACCEPTED;
        CodeExecutionResult lastResult = null;

        for (CodingProblemTestCase testCase : testCases) {
            CodeExecutionResult result = codeExecutionClient.execute(
                    request.getLanguage(),
                    request.getSourceCode(),
                    testCase.getInput(),
                    testCase.getExpectedOutput(),
                    problem.getTimeLimitMillis(),
                    problem.getMemoryLimitMb()
            );
            lastResult = result;

            if (result.status() == CodingSubmissionStatus.ACCEPTED) {
                passedCount++;
                continue;
            }

            finalStatus = result.status();
            break;
        }

        CodingSubmission submission = codingSubmissionRepository.save(CodingSubmission.builder()
                .user(user)
                .problem(problem)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .status(finalStatus)
                .passedCount(passedCount)
                .totalCount(testCases.size())
                .judgeToken(lastResult == null ? null : lastResult.token())
                .stdout(lastResult == null ? null : lastResult.stdout())
                .stderr(lastResult == null ? null : lastResult.stderr())
                .compileOutput(lastResult == null ? null : lastResult.compileOutput())
                .build());

        return CodingSubmissionResponse.of(submission, lastResult);
    }

    private CodingProblem getPublishedProblem(Long problemId) {
        return codingProblemRepository
                .findByIdAndStatusAndDeletedAtIsNull(problemId, CodingProblemStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_PROBLEM_NOT_FOUND));
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input;
    }
}
