package com.cobip.dto.mypage;

import java.time.LocalDate;

import com.cobip.domain.subscription.Subscription;
import com.cobip.domain.subscription.SubscriptionStatus;

import lombok.Getter;

@Getter
public class SubscriptionResponse {

    private final String planName;
    private final SubscriptionStatus status;
    private final LocalDate startedAt;
    private final LocalDate expiredAt;
    private final LocalDate nextPaymentAt;
    private final boolean active;

    private SubscriptionResponse(Subscription subscription) {
        this.planName = subscription.getPlanName();
        this.status = subscription.getStatus();
        this.startedAt = subscription.getStartedAt();
        this.expiredAt = subscription.getExpiredAt();
        this.nextPaymentAt = subscription.getNextPaymentAt();
        this.active = subscription.isActive();
    }

    private SubscriptionResponse() {
        this.planName = null;
        this.status = null;
        this.startedAt = null;
        this.expiredAt = null;
        this.nextPaymentAt = null;
        this.active = false;
    }

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(subscription);
    }

    public static SubscriptionResponse none() {
        return new SubscriptionResponse();
    }
}
