package com.cobip.domain.coding;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.cobip.dto.admin.AdminCodingProblemCreateRequest;
import com.cobip.dto.admin.AdminCodingProblemDetailResponse;
import com.cobip.dto.admin.AdminCodingProblemSummaryResponse;
import com.cobip.dto.admin.AdminCodingProblemStarterCodeRequest;
import com.cobip.dto.admin.AdminCodingProblemTestCaseRequest;
import com.cobip.dto.admin.AdminCodingProblemUpdateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookCreateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookDetailResponse;
import com.cobip.dto.admin.AdminCodingWorkbookSummaryResponse;
import com.cobip.dto.admin.AdminCodingWorkbookUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCodingWorkbookService {

    private final CodingWorkbookRepository codingWorkbookRepository;
    private final CodingProblemRepository codingProblemRepository;
    private final CodingProblemTestCaseRepository codingProblemTestCaseRepository;
    private final CodingProblemStarterCodeRepository codingProblemStarterCodeRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminCodingWorkbookSummaryResponse> getWorkbooks(
        String keyword,
        String category,
        CodingDifficulty difficulty,
        CodingWorkbookStatus status,
        Pageable pageable
    ) {
        return PageResponse.from(codingWorkbookRepository.findAll(
                workbookSpec(keyword, category, difficulty, status),
                pageable
        ).map(AdminCodingWorkbookSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminCodingWorkbookDetailResponse getWorkbook(Long workbookId) {
        CodingWorkbook workbook = findWorkbook(workbookId);
        List<CodingProblem> problems = codingProblemRepository
                .findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(workbook.getId());
        return AdminCodingWorkbookDetailResponse.of(workbook, problems);
    }

    @Transactional
    public AdminCodingWorkbookDetailResponse createWorkbook(AdminCodingWorkbookCreateRequest request) {
        validateUniqueSlug(request.getSlug(), null);

        CodingWorkbook workbook = codingWorkbookRepository.save(CodingWorkbook.builder()
                .slug(request.getSlug())
                .title(request.getTitle())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .summary(request.getSummary())
                .description(request.getDescription())
                .status(defaultWorkbookStatus(request.getStatus()))
                .displayOrder(request.getDisplayOrder())
                .build());

        return AdminCodingWorkbookDetailResponse.of(workbook, List.of());
    }

    @Transactional
    public AdminCodingWorkbookDetailResponse updateWorkbook(
        Long workbookId,
        AdminCodingWorkbookUpdateRequest request
    ) {
        CodingWorkbook workbook = findWorkbook(workbookId);
        if (request.getSlug() != null) {
            validateUniqueSlug(request.getSlug(), workbook.getId());
        }

        workbook.update(
                request.getSlug(),
                request.getTitle(),
                request.getCategory(),
                request.getDifficulty(),
                request.getSummary(),
                request.getDescription(),
                request.getStatus(),
                request.getDisplayOrder()
        );
        List<CodingProblem> problems = codingProblemRepository
                .findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(workbook.getId());
        return AdminCodingWorkbookDetailResponse.of(workbook, problems);
    }

    @Transactional
    public void deleteWorkbook(Long workbookId) {
        CodingWorkbook workbook = findWorkbook(workbookId);
        workbook.delete();
        codingProblemRepository.findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(workbook.getId())
                .forEach(CodingProblem::delete);
    }

    @Transactional(readOnly = true)
    public List<AdminCodingProblemSummaryResponse> getProblems(Long workbookId) {
        CodingWorkbook workbook = findWorkbook(workbookId);
        return codingProblemRepository.findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(workbook.getId())
                .stream()
                .map(AdminCodingProblemSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCodingProblemDetailResponse getProblem(Long workbookId, Long problemId) {
        CodingProblem problem = findProblem(workbookId, problemId);
        return problemDetail(problem);
    }

    @Transactional
    public AdminCodingProblemDetailResponse createProblem(
        Long workbookId,
        AdminCodingProblemCreateRequest request
    ) {
        CodingWorkbook workbook = findWorkbook(workbookId);
        validateUniqueStarterLanguages(request.getStarterCodes());

        CodingProblem problem = codingProblemRepository.save(CodingProblem.builder()
                .workbook(workbook)
                .title(request.getTitle())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .contentJson(request.getContentJson())
                .explanationJson(request.getExplanationJson())
                .orderIndex(request.getOrderIndex())
                .timeLimitMillis(request.getTimeLimitMillis())
                .memoryLimitMb(request.getMemoryLimitMb())
                .status(defaultProblemStatus(request.getStatus()))
                .build());
        replaceTestCases(problem, request.getTestCases());
        replaceStarterCodes(problem, request.getStarterCodes());

        return problemDetail(problem);
    }

    @Transactional
    public AdminCodingProblemDetailResponse updateProblem(
        Long workbookId,
        Long problemId,
        AdminCodingProblemUpdateRequest request
    ) {
        CodingProblem problem = findProblem(workbookId, problemId);
        if (request.getStarterCodes() != null) {
            validateUniqueStarterLanguages(request.getStarterCodes());
        }

        problem.update(
                request.getTitle(),
                request.getCategory(),
                request.getDifficulty(),
                request.getContentJson(),
                request.getExplanationJson(),
                request.getOrderIndex(),
                request.getTimeLimitMillis(),
                request.getMemoryLimitMb(),
                request.getStatus()
        );
        if (request.getTestCases() != null) {
            replaceTestCases(problem, request.getTestCases());
        }
        if (request.getStarterCodes() != null) {
            replaceStarterCodes(problem, request.getStarterCodes());
        }

        return problemDetail(problem);
    }

    @Transactional
    public void deleteProblem(Long workbookId, Long problemId) {
        findProblem(workbookId, problemId).delete();
    }

    private AdminCodingProblemDetailResponse problemDetail(CodingProblem problem) {
        List<CodingProblemTestCase> testCases = codingProblemTestCaseRepository
                .findByProblemIdOrderByOrderIndexAsc(problem.getId());
        List<CodingProblemStarterCode> starterCodes = codingProblemStarterCodeRepository
                .findByProblemIdOrderByLanguageAsc(problem.getId());
        return AdminCodingProblemDetailResponse.of(problem, testCases, starterCodes);
    }

    private void replaceTestCases(
        CodingProblem problem,
        List<AdminCodingProblemTestCaseRequest> requests
    ) {
        codingProblemTestCaseRepository.deleteAllInBatch(
                codingProblemTestCaseRepository.findByProblemIdOrderByOrderIndexAsc(problem.getId())
        );
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<CodingProblemTestCase> testCases = requests.stream()
                .map(request -> CodingProblemTestCase.builder()
                        .problem(problem)
                        .input(request.getInput())
                        .expectedOutput(request.getExpectedOutput())
                        .sample(Boolean.TRUE.equals(request.getSample()))
                        .orderIndex(request.getOrderIndex())
                        .build())
                .toList();
        codingProblemTestCaseRepository.saveAll(testCases);
    }

    private void replaceStarterCodes(
        CodingProblem problem,
        List<AdminCodingProblemStarterCodeRequest> requests
    ) {
        codingProblemStarterCodeRepository.deleteAllInBatch(
                codingProblemStarterCodeRepository.findByProblemIdOrderByLanguageAsc(problem.getId())
        );
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<CodingProblemStarterCode> starterCodes = requests.stream()
                .map(request -> CodingProblemStarterCode.builder()
                        .problem(problem)
                        .language(request.getLanguage())
                        .code(request.getCode())
                        .build())
                .toList();
        codingProblemStarterCodeRepository.saveAll(starterCodes);
    }

    private void validateUniqueSlug(String slug, Long workbookId) {
        boolean duplicated = workbookId == null
                ? codingWorkbookRepository.existsBySlugAndDeletedAtIsNull(slug)
                : codingWorkbookRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, workbookId);
        if (duplicated) {
            throw new CustomException(ErrorCode.DUPLICATE_CODING_WORKBOOK_SLUG);
        }
    }

    private void validateUniqueStarterLanguages(List<AdminCodingProblemStarterCodeRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        Set<CodingLanguage> languages = new HashSet<>();
        for (AdminCodingProblemStarterCodeRequest request : requests) {
            if (!languages.add(request.getLanguage())) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private CodingWorkbook findWorkbook(Long workbookId) {
        return codingWorkbookRepository.findByIdAndDeletedAtIsNull(workbookId)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_WORKBOOK_NOT_FOUND));
    }

    private CodingProblem findProblem(Long workbookId, Long problemId) {
        findWorkbook(workbookId);
        return codingProblemRepository.findByIdAndWorkbookIdAndDeletedAtIsNull(problemId, workbookId)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_PROBLEM_NOT_FOUND));
    }

    private CodingWorkbookStatus defaultWorkbookStatus(CodingWorkbookStatus status) {
        return status == null ? CodingWorkbookStatus.DRAFT : status;
    }

    private CodingProblemStatus defaultProblemStatus(CodingProblemStatus status) {
        return status == null ? CodingProblemStatus.DRAFT : status;
    }

    private Specification<CodingWorkbook> workbookSpec(
        String keyword,
        String category,
        CodingDifficulty difficulty,
        CodingWorkbookStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.isNull(root.get("deletedAt"));

            if (category != null && !category.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), likeKeyword)
                ));
            }

            return predicate;
        };
    }
}
