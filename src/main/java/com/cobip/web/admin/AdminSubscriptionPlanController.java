package com.cobip.web.admin;

import com.cobip.domain.subscription.SubscriptionPlanService;
import com.cobip.dto.admin.SubscriptionPlanCreateRequest;
import com.cobip.dto.admin.SubscriptionPlanResponse;
import com.cobip.dto.admin.SubscriptionPlanUpdateRequest;
import com.cobip.dto.admin.SubscriptionPlanVisibilityUpdateRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription-plans")
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
        @RequestBody @Valid SubscriptionPlanCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subscription plan created.",
                subscriptionPlanService.createPlan(request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionPlanResponse>>> getPlans(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean visible,
        @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getPlans(keyword, visible, pageable)));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getPlan(planId)));
    }

    @PatchMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
        @PathVariable Long planId,
        @RequestBody @Valid SubscriptionPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subscription plan updated.",
                subscriptionPlanService.updatePlan(planId, request)
        ));
    }

    @PatchMapping("/{planId}/visibility")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> changeVisibility(
        @PathVariable Long planId,
        @RequestBody @Valid SubscriptionPlanVisibilityUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subscription plan visibility updated.",
                subscriptionPlanService.changeVisibility(planId, request)
        ));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long planId) {
        subscriptionPlanService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted.", null));
    }
}
