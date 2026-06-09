package com.cobip.domain.coding;

import java.util.Locale;
import java.util.Set;
import java.util.Collections;

import com.cobip.domain.user.User;
import com.cobip.dto.coding.CodingWorkbookDetailResponse;
import com.cobip.dto.coding.CodingWorkbookSummaryResponse;
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
public class CodingWorkbookService {

    private final CodingWorkbookRepository codingWorkbookRepository;
    private final CodingProblemRepository codingProblemRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;

    @Transactional(readOnly = true)
    public PageResponse<CodingWorkbookSummaryResponse> getWorkbooks(
        String keyword,
        String category,
        CodingDifficulty difficulty,
        Pageable pageable
    ) {
        return PageResponse.from(codingWorkbookRepository.findAll(
                publicWorkbookSpec(keyword, category, difficulty),
                pageable
        ).map(CodingWorkbookSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public CodingWorkbookDetailResponse getWorkbook(User user, Long workbookId) {
        CodingWorkbook workbook = codingWorkbookRepository
                .findByIdAndStatusAndDeletedAtIsNull(workbookId, CodingWorkbookStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_WORKBOOK_NOT_FOUND));
        var problems = codingProblemRepository.findByWorkbookIdAndStatusAndDeletedAtIsNullOrderByOrderIndexAsc(
                workbook.getId(),
                CodingProblemStatus.PUBLISHED
        );
        Set<Long> solvedProblemIds = problems.isEmpty()
                ? Collections.emptySet()
                : codingSubmissionRepository.findSolvedProblemIdsByUserIdAndProblemIds(
                        user.getId(),
                        problems.stream()
                                .map(CodingProblem::getId)
                                .toList(),
                        CodingSubmissionStatus.ACCEPTED
                );
        return CodingWorkbookDetailResponse.of(workbook, problems, solvedProblemIds);
    }

    private Specification<CodingWorkbook> publicWorkbookSpec(
        String keyword,
        String category,
        CodingDifficulty difficulty
    ) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }

            Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("status"), CodingWorkbookStatus.PUBLISHED),
                    criteriaBuilder.isNull(root.get("deletedAt"))
            );

            if (category != null && !category.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), likeKeyword)
                ));
            }

            return predicate;
        };
    }
}
