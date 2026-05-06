package com.cobip.domain.activity;

import java.util.ArrayList;
import java.util.List;

import com.cobip.dto.admin.AdminActivityHistoryResponse;
import com.cobip.global.common.PageResponse;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminActivityHistoryService {

    private final ActivityHistoryRepository activityHistoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminActivityHistoryResponse> getActivityHistories(
        Long userId,
        ActivityType type,
        String targetType,
        Pageable pageable
    ) {
        return PageResponse.from(activityHistoryRepository.findAll(
                activityHistorySpec(userId, type, targetType),
                pageable
        ).map(AdminActivityHistoryResponse::from));
    }

    private Specification<ActivityHistory> activityHistorySpec(
        Long userId,
        ActivityType type,
        String targetType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("targetType"), targetType.trim()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
