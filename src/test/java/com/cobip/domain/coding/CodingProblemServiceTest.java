package com.cobip.domain.coding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import com.cobip.global.common.PageResponse;

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
