package com.cobip.dto.subscription;

import com.cobip.domain.subscription.SubscriptionPlan;

import lombok.Getter;

@Getter
public class SubscriptionPlanPublicResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final int priceAmount;
    private final String currency;
    private final int durationDays;
    private final String benefitDescription;

    private SubscriptionPlanPublicResponse(SubscriptionPlan plan) {
        this.id = plan.getId();
        this.code = plan.getCode();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.priceAmount = plan.getPriceAmount();
        this.currency = plan.getCurrency();
        this.durationDays = plan.getDurationDays();
        this.benefitDescription = plan.getBenefitDescription();
    }

    public static SubscriptionPlanPublicResponse from(SubscriptionPlan plan) {
        return new SubscriptionPlanPublicResponse(plan);
    }
}
