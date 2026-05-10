package com.cobip.domain.coding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.dto.admin.AdminCodingProblemCreateRequest;
import com.cobip.dto.admin.AdminCodingProblemStarterCodeRequest;
import com.cobip.dto.admin.AdminCodingProblemTestCaseRequest;
import com.cobip.dto.admin.AdminCodingWorkbookCreateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminCodingWorkbookServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private CodingWorkbookRepository codingWorkbookRepository;

    @Mock
    private CodingProblemRepository codingProblemRepository;

    @Mock
    private CodingProblemTestCaseRepository codingProblemTestCaseRepository;

    @Mock
    private CodingProblemStarterCodeRepository codingProblemStarterCodeRepository;

    private AdminCodingWorkbookService adminCodingWorkbookService;

    @BeforeEach
    void setUp() {
        adminCodingWorkbookService = new AdminCodingWorkbookService(
                codingWorkbookRepository,
                codingProblemRepository,
                codingProblemTestCaseRepository,
                codingProblemStarterCodeRepository
        );
    }

    @Test
    void createWorkbookRejectsDuplicateSlug() {
        AdminCodingWorkbookCreateRequest request = workbookCreateRequest();
        when(codingWorkbookRepository.existsBySlugAndDeletedAtIsNull("basic-array")).thenReturn(true);

        assertThatThrownBy(() -> adminCodingWorkbookService.createWorkbook(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_CODING_WORKBOOK_SLUG);
    }

    @Test
    void createProblemStoresProblemAssets() {
        CodingWorkbook workbook = workbook(1L);
        AdminCodingProblemCreateRequest request = problemCreateRequest();
        when(codingWorkbookRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(workbook));
        when(codingProblemRepository.save(any(CodingProblem.class))).thenAnswer(invocation -> {
            CodingProblem problem = invocation.getArgument(0);
            ReflectionTestUtils.setField(problem, "id", 10L);
            return problem;
        });
        when(codingProblemTestCaseRepository.findByProblemIdOrderByOrderIndexAsc(10L)).thenReturn(List.of());
        when(codingProblemStarterCodeRepository.findByProblemIdOrderByLanguageAsc(10L)).thenReturn(List.of());

        var response = adminCodingWorkbookService.createProblem(1L, request);

        assertThat(response.getId()).isEqualTo(10L);
        verify(codingProblemTestCaseRepository).saveAll(any());
        verify(codingProblemStarterCodeRepository).saveAll(any());
    }

    @Test
    void deleteWorkbookSoftDeletesWorkbookAndProblems() {
        CodingWorkbook workbook = workbook(1L);
        CodingProblem problem = problem(10L, workbook);
        when(codingWorkbookRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(workbook));
        when(codingProblemRepository.findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(1L))
                .thenReturn(List.of(problem));

        adminCodingWorkbookService.deleteWorkbook(1L);

        assertThat(workbook.getDeletedAt()).isNotNull();
        assertThat(problem.getDeletedAt()).isNotNull();
    }

    private AdminCodingWorkbookCreateRequest workbookCreateRequest() {
        AdminCodingWorkbookCreateRequest request = new AdminCodingWorkbookCreateRequest();
        ReflectionTestUtils.setField(request, "slug", "basic-array");
        ReflectionTestUtils.setField(request, "title", "Basic Array");
        ReflectionTestUtils.setField(request, "category", "algorithm");
        ReflectionTestUtils.setField(request, "difficulty", CodingDifficulty.EASY);
        ReflectionTestUtils.setField(request, "summary", "Array basics");
        ReflectionTestUtils.setField(request, "description", "Practice arrays.");
        ReflectionTestUtils.setField(request, "status", CodingWorkbookStatus.DRAFT);
        ReflectionTestUtils.setField(request, "displayOrder", 1);
        return request;
    }

    private AdminCodingProblemCreateRequest problemCreateRequest() {
        AdminCodingProblemCreateRequest request = new AdminCodingProblemCreateRequest();
        ReflectionTestUtils.setField(request, "title", "Two Sum");
        ReflectionTestUtils.setField(request, "category", "algorithm");
        ReflectionTestUtils.setField(request, "difficulty", CodingDifficulty.EASY);
        ReflectionTestUtils.setField(request, "contentJson", OBJECT_MAPPER.createObjectNode());
        ReflectionTestUtils.setField(request, "explanationJson", OBJECT_MAPPER.createObjectNode());
        ReflectionTestUtils.setField(request, "orderIndex", 1);
        ReflectionTestUtils.setField(request, "timeLimitMillis", 2000);
        ReflectionTestUtils.setField(request, "memoryLimitMb", 256);
        ReflectionTestUtils.setField(request, "status", CodingProblemStatus.DRAFT);
        ReflectionTestUtils.setField(request, "testCases", List.of(testCaseRequest()));
        ReflectionTestUtils.setField(request, "starterCodes", List.of(starterCodeRequest()));
        return request;
    }

    private AdminCodingProblemTestCaseRequest testCaseRequest() {
        AdminCodingProblemTestCaseRequest request = new AdminCodingProblemTestCaseRequest();
        ReflectionTestUtils.setField(request, "input", "1 1");
        ReflectionTestUtils.setField(request, "expectedOutput", "2");
        ReflectionTestUtils.setField(request, "sample", true);
        ReflectionTestUtils.setField(request, "orderIndex", 1);
        return request;
    }

    private AdminCodingProblemStarterCodeRequest starterCodeRequest() {
        AdminCodingProblemStarterCodeRequest request = new AdminCodingProblemStarterCodeRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "code", "print('hello')");
        return request;
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
                .contentJson(OBJECT_MAPPER.createObjectNode())
                .orderIndex(1)
                .timeLimitMillis(2000)
                .memoryLimitMb(256)
                .status(CodingProblemStatus.PUBLISHED)
                .build();
    }
}
