package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.subscription.SubscriptionPlan;

import lombok.Getter;

@Getter
public class SubscriptionPlanResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final int priceAmount;
    private final String currency;
    private final int durationDays;
    private final String benefitDescription;
    private final boolean visible;
    private final int displayOrder;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private SubscriptionPlanResponse(SubscriptionPlan plan) {
        this.id = plan.getId();
        this.code = plan.getCode();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.priceAmount = plan.getPriceAmount();
        this.currency = plan.getCurrency();
        this.durationDays = plan.getDurationDays();
        this.benefitDescription = plan.getBenefitDescription();
        this.visible = plan.isVisible();
        this.displayOrder = plan.getDisplayOrder();
        this.createdAt = plan.getCreatedAt();
        this.updatedAt = plan.getUpdatedAt();
    }

    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(plan);
    }
}
