package com.cobip.domain.coding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.coding.CodingCodeRunRequest;
import com.cobip.dto.coding.CodingSubmissionRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CodingSubmissionServiceTest {

    @Mock
    private CodingProblemRepository codingProblemRepository;

    @Mock
    private CodingProblemTestCaseRepository codingProblemTestCaseRepository;

    @Mock
    private CodingSubmissionRepository codingSubmissionRepository;

    @Mock
    private CodeExecutionClient codeExecutionClient;

    private CodingSubmissionService codingSubmissionService;

    @BeforeEach
    void setUp() {
        codingSubmissionService = new CodingSubmissionService(
                codingProblemRepository,
                codingProblemTestCaseRepository,
                codingSubmissionRepository,
                codeExecutionClient
        );
    }

    @Test
    void runProblemExecutesCodeWithCustomInput() {
        CodingProblem problem = problem(10L);
        CodingCodeRunRequest request = runRequest();
        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(problem));
        when(codeExecutionClient.execute(
                eq(CodingLanguage.PYTHON),
                eq("print(input())"),
                eq("hello"),
                eq(null),
                eq(2000),
                eq(256)
        )).thenReturn(executionResult(CodingSubmissionStatus.ACCEPTED));

        var response = codingSubmissionService.runProblem(10L, request);

        assertThat(response.getStatus()).isEqualTo(CodingSubmissionStatus.ACCEPTED);
        assertThat(response.getStdout()).isEqualTo("ok");
    }

    @Test
    void submitProblemSavesAcceptedSubmissionWhenAllTestCasesPass() {
        CodingProblem problem = problem(10L);
        CodingProblemTestCase first = testCase(1L, problem, "1 1", "2");
        CodingProblemTestCase second = testCase(2L, problem, "2 3", "5");
        CodingSubmissionRequest request = submitRequest();
        User user = user(1L);

        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(problem));
        when(codingProblemTestCaseRepository.findByProblemIdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(first, second));
        when(codeExecutionClient.execute(eq(CodingLanguage.PYTHON), eq("print(sum(map(int, input().split())))"), any(), any(), eq(2000), eq(256)))
                .thenReturn(executionResult(CodingSubmissionStatus.ACCEPTED));
        when(codingSubmissionRepository.save(any(CodingSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = codingSubmissionService.submitProblem(user, 10L, request);

        assertThat(response.getStatus()).isEqualTo(CodingSubmissionStatus.ACCEPTED);
        assertThat(response.getPassedCount()).isEqualTo(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
    }

    @Test
    void submitProblemStopsAtFirstFailedTestCase() {
        CodingProblem problem = problem(10L);
        CodingProblemTestCase first = testCase(1L, problem, "1 1", "2");
        CodingProblemTestCase second = testCase(2L, problem, "2 3", "5");
        CodingSubmissionRequest request = submitRequest();
        User user = user(1L);

        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(problem));
        when(codingProblemTestCaseRepository.findByProblemIdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(first, second));
        when(codeExecutionClient.execute(eq(CodingLanguage.PYTHON), eq("print(sum(map(int, input().split())))"), eq("1 1"), eq("2"), eq(2000), eq(256)))
                .thenReturn(executionResult(CodingSubmissionStatus.ACCEPTED));
        when(codeExecutionClient.execute(eq(CodingLanguage.PYTHON), eq("print(sum(map(int, input().split())))"), eq("2 3"), eq("5"), eq(2000), eq(256)))
                .thenReturn(executionResult(CodingSubmissionStatus.WRONG_ANSWER));
        when(codingSubmissionRepository.save(any(CodingSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = codingSubmissionService.submitProblem(user, 10L, request);

        assertThat(response.getStatus()).isEqualTo(CodingSubmissionStatus.WRONG_ANSWER);
        assertThat(response.getPassedCount()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(2);
    }

    @Test
    void submitProblemRejectsProblemWithoutTestCases() {
        CodingProblem problem = problem(10L);
        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(problem));
        when(codingProblemTestCaseRepository.findByProblemIdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> codingSubmissionService.submitProblem(user(1L), 10L, submitRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private CodingCodeRunRequest runRequest() {
        CodingCodeRunRequest request = new CodingCodeRunRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "sourceCode", "print(input())");
        ReflectionTestUtils.setField(request, "input", "hello");
        return request;
    }

    private CodingSubmissionRequest submitRequest() {
        CodingSubmissionRequest request = new CodingSubmissionRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "sourceCode", "print(sum(map(int, input().split())))");
        return request;
    }

    private CodeExecutionResult executionResult(CodingSubmissionStatus status) {
        return new CodeExecutionResult(status, "token", "ok", null, null, null, "0.01", 1024);
    }

    private CodingProblem problem(Long id) {
        return CodingProblem.builder()
                .id(id)
                .workbook(workbook(1L))
                .title("A+B")
                .category("io")
                .difficulty(CodingDifficulty.EASY)
                .orderIndex(1)
                .timeLimitMillis(2000)
                .memoryLimitMb(256)
                .status(CodingProblemStatus.PUBLISHED)
                .build();
    }

    private CodingWorkbook workbook(Long id) {
        return CodingWorkbook.builder()
                .id(id)
                .slug("basic-io")
                .title("Basic IO")
                .category("io")
                .difficulty(CodingDifficulty.EASY)
                .summary("basic io")
                .status(CodingWorkbookStatus.PUBLISHED)
                .displayOrder(1)
                .build();
    }

    private CodingProblemTestCase testCase(Long id, CodingProblem problem, String input, String expectedOutput) {
        return CodingProblemTestCase.builder()
                .id(id)
                .problem(problem)
                .input(input)
                .expectedOutput(expectedOutput)
                .sample(false)
                .orderIndex(id.intValue())
                .build();
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
