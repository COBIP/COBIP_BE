package com.cobip.domain.subscription;

import java.util.ArrayList;
import java.util.List;

import com.cobip.dto.admin.SubscriptionPlanCreateRequest;
import com.cobip.dto.admin.SubscriptionPlanResponse;
import com.cobip.dto.admin.SubscriptionPlanUpdateRequest;
import com.cobip.dto.admin.SubscriptionPlanVisibilityUpdateRequest;
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
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request) {
        validateUniqueCode(request.getCode());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlan.create(request));
        return SubscriptionPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionPlanResponse> getPlans(String keyword, Boolean visible, Pageable pageable) {
        return PageResponse.from(subscriptionPlanRepository.findAll(
                planSpec(keyword, visible),
                pageable
        ).map(SubscriptionPlanResponse::from));
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlan(Long planId) {
        return SubscriptionPlanResponse.from(findPlan(planId));
    }

    @Transactional
    public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanUpdateRequest request) {
        SubscriptionPlan plan = findPlan(planId);
        if (request.getCode() != null && !request.getCode().equals(plan.getCode())) {
            validateUniqueCode(request.getCode(), plan.getId());
        }
        plan.update(request);
        return SubscriptionPlanResponse.from(plan);
    }

    @Transactional
    public SubscriptionPlanResponse changeVisibility(Long planId, SubscriptionPlanVisibilityUpdateRequest request) {
        SubscriptionPlan plan = findPlan(planId);
        plan.changeVisibility(request.isVisible());
        return SubscriptionPlanResponse.from(plan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        SubscriptionPlan plan = findPlan(planId);
        plan.delete();
    }

    private SubscriptionPlan findPlan(Long planId) {
        return subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));
    }

    private void validateUniqueCode(String code) {
        if (subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull(code)) {
            throw new CustomException(ErrorCode.DUPLICATE_SUBSCRIPTION_PLAN_CODE);
        }
    }

    private void validateUniqueCode(String code, Long planId) {
        if (subscriptionPlanRepository.existsByCodeAndDeletedAtIsNullAndIdNot(code, planId)) {
            throw new CustomException(ErrorCode.DUPLICATE_SUBSCRIPTION_PLAN_CODE);
        }
    }

    private Specification<SubscriptionPlan> planSpec(String keyword, Boolean visible) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword)
                ));
            }
            if (visible != null) {
                predicates.add(criteriaBuilder.equal(root.get("visible"), visible));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
