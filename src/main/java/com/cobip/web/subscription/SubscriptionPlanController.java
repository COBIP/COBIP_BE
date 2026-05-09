package com.cobip.web.subscription;

import com.cobip.domain.subscription.SubscriptionPlanService;
import com.cobip.dto.subscription.SubscriptionPlanPublicResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription-plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionPlanPublicResponse>>> getPlans(
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getVisiblePlans(keyword, pageable)));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanPublicResponse>> getPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getVisiblePlan(planId)));
    }
}
