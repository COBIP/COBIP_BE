package com.cobip.domain.coding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CodingWorkbookServiceTest {

    @Mock
    private CodingWorkbookRepository codingWorkbookRepository;

    @Mock
    private CodingProblemRepository codingProblemRepository;

    private CodingWorkbookService codingWorkbookService;

    @BeforeEach
    void setUp() {
        codingWorkbookService = new CodingWorkbookService(codingWorkbookRepository, codingProblemRepository);
    }

    @Test
    void getWorkbooksReturnsPagedWorkbookSummaries() {
        CodingWorkbook workbook = workbook(1L);
        when(codingWorkbookRepository.findAll(
                anyWorkbookSpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(workbook)));

        PageResponse<?> response = codingWorkbookService.getWorkbooks(
                "array",
                "algorithm",
                CodingDifficulty.EASY,
                PageRequest.of(0, 20)
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getWorkbookReturnsPublishedWorkbookWithProblems() {
        CodingWorkbook workbook = workbook(1L);
        CodingProblem problem = problem(10L, workbook);
        when(codingWorkbookRepository.findByIdAndStatusAndDeletedAtIsNull(1L, CodingWorkbookStatus.PUBLISHED))
                .thenReturn(Optional.of(workbook));
        when(codingProblemRepository.findByWorkbookIdAndStatusAndDeletedAtIsNullOrderByOrderIndexAsc(
                1L,
                CodingProblemStatus.PUBLISHED
        )).thenReturn(List.of(problem));

        var response = codingWorkbookService.getWorkbook(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProblems()).hasSize(1);
        assertThat(response.getProblems().get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getWorkbookRejectsMissingWorkbook() {
        when(codingWorkbookRepository.findByIdAndStatusAndDeletedAtIsNull(1L, CodingWorkbookStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingWorkbookService.getWorkbook(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODING_WORKBOOK_NOT_FOUND);
    }

    private Specification<CodingWorkbook> anyWorkbookSpecification() {
        return any();
    }

    private CodingWorkbook workbook(Long id) {
        return CodingWorkbook.builder()
                .id(id)
                .slug("basic-array")
                .title("Basic Array")
                .category("algorithm")
                .difficulty(CodingDifficulty.EASY)
                .summary("Array basics")
                .description("Practice arrays.")
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
}
