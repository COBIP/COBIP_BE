package com.cobip.domain.coding;

import java.util.Locale;

import com.cobip.dto.coding.CodingProblemSummaryResponse;
import com.cobip.dto.coding.CodingProblemDetailResponse;
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
public class CodingProblemService {

    private final CodingProblemRepository codingProblemRepository;

    @Transactional(readOnly = true)
    public PageResponse<CodingProblemSummaryResponse> getProblems(
        String keyword,
        String category,
        CodingProblemDifficulty difficulty,
        Pageable pageable
    ) {
        return PageResponse.from(codingProblemRepository.findAll(
                publicProblemSpec(keyword, category, difficulty),
                pageable
        ).map(CodingProblemSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public CodingProblemDetailResponse getProblem(Long problemId) {
        CodingProblem problem = codingProblemRepository.findByIdAndPublishedTrueAndDeletedAtIsNull(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CODING_PROBLEM_NOT_FOUND));
        return CodingProblemDetailResponse.from(problem);
    }

    private Specification<CodingProblem> publicProblemSpec(
        String keyword,
        String category,
        CodingProblemDifficulty difficulty
    ) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }

            Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("published")),
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
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword)
                ));
            }

            return predicate;
        };
    }
}
