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
class CodingProblemServiceTest {

    @Mock
    private CodingProblemRepository codingProblemRepository;

    private CodingProblemService codingProblemService;

    @BeforeEach
    void setUp() {
        codingProblemService = new CodingProblemService(codingProblemRepository);
    }

    @Test
    void getProblemsReturnsPagedProblemSummaries() {
        CodingProblem problem = problem(1L);
        when(codingProblemRepository.findAll(
                anyProblemSpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(problem)));

        PageResponse<?> response = codingProblemService.getProblems(
                "sum",
                "array",
                CodingProblemDifficulty.EASY,
                PageRequest.of(0, 20)
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getProblemReturnsPublishedProblemDetail() {
        CodingProblem problem = problem(1L);
        when(codingProblemRepository.findByIdAndPublishedTrueAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(problem));

        var response = codingProblemService.getProblem(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Two Sum");
        assertThat(response.getSampleOutput()).isEqualTo("2");
    }

    @Test
    void getProblemRejectsMissingProblem() {
        when(codingProblemRepository.findByIdAndPublishedTrueAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingProblemService.getProblem(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODING_PROBLEM_NOT_FOUND);
    }

    private Specification<CodingProblem> anyProblemSpecification() {
        return any();
    }

    private CodingProblem problem(Long id) {
        return CodingProblem.builder()
                .id(id)
                .title("Two Sum")
                .category("array")
                .difficulty(CodingProblemDifficulty.EASY)
                .description("Find two numbers.")
                .inputDescription("Two integers.")
                .outputDescription("Sum.")
                .sampleInput("1 1")
                .sampleOutput("2")
                .timeLimitMillis(2000)
                .memoryLimitMb(256)
                .published(true)
                .build();
    }
}
