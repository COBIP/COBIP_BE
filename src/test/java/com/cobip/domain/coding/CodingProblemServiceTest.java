package com.cobip.domain.coding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodingProblemServiceTest {

    @Mock
    private CodingProblemRepository codingProblemRepository;

    @Mock
    private CodingProblemTestCaseRepository codingProblemTestCaseRepository;

    @Mock
    private CodingProblemStarterCodeRepository codingProblemStarterCodeRepository;

    private CodingProblemService codingProblemService;

    @BeforeEach
    void setUp() {
        codingProblemService = new CodingProblemService(
                codingProblemRepository,
                codingProblemTestCaseRepository,
                codingProblemStarterCodeRepository
        );
    }

    @Test
    void getProblemReturnsPublishedProblemDetail() {
        CodingWorkbook workbook = workbook(1L);
        CodingProblem problem = problem(10L, workbook);
        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(problem));
        when(codingProblemTestCaseRepository.findByProblemIdAndSampleOrderByOrderIndexAsc(10L, true))
                .thenReturn(List.of(sampleTestCase(100L, problem)));
        when(codingProblemStarterCodeRepository.findByProblemIdOrderByLanguageAsc(10L))
                .thenReturn(List.of(starterCode(200L, problem)));

        var response = codingProblemService.getProblem(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getWorkbookId()).isEqualTo(1L);
        assertThat(response.getSampleTestCases()).hasSize(1);
        assertThat(response.getStarterCodes()).hasSize(1);
    }

    @Test
    void getProblemRejectsMissingProblem() {
        when(codingProblemRepository.findByIdAndStatusAndDeletedAtIsNull(10L, CodingProblemStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingProblemService.getProblem(10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODING_PROBLEM_NOT_FOUND);
    }

    private CodingWorkbook workbook(Long id) {
        return CodingWorkbook.builder()
                .id(id)
                .slug("basic-array")
                .title("Basic Array")
                .category("algorithm")
                .difficulty(CodingDifficulty.EASY)
                .summary("Array basics")
                .status(CodingWorkbookStatus.PUBLISHED)
                .displayOrder(1)
                .build();
    }

    private CodingProblem problem(Long id, CodingWorkbook workbook) {
        return CodingProblem.builder()
                .id(id)
                .workbook(workbook)
                .title("Two Sum")
                .category("algorithm")
                .difficulty(CodingDifficulty.EASY)
                .orderIndex(1)
                .timeLimitMillis(2000)
                .memoryLimitMb(256)
                .status(CodingProblemStatus.PUBLISHED)
                .build();
    }

    private CodingProblemTestCase sampleTestCase(Long id, CodingProblem problem) {
        return CodingProblemTestCase.builder()
                .id(id)
                .problem(problem)
                .input("1 1")
                .expectedOutput("2")
                .sample(true)
                .orderIndex(1)
                .build();
    }

    private CodingProblemStarterCode starterCode(Long id, CodingProblem problem) {
        return CodingProblemStarterCode.builder()
                .id(id)
                .problem(problem)
                .language(CodingLanguage.JAVA)
                .code("class Main {}")
                .build();
    }
}
