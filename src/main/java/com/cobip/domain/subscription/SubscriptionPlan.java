package com.cobip.domain.subscription;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.dto.admin.SubscriptionPlanCreateRequest;
import com.cobip.dto.admin.SubscriptionPlanUpdateRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_plans")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int priceAmount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private int durationDays;

    @Lob
    @Column(nullable = false)
    private String benefitDescription;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private int displayOrder;

    private LocalDateTime deletedAt;

    public static SubscriptionPlan create(SubscriptionPlanCreateRequest request) {
        return SubscriptionPlan.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .priceAmount(request.getPriceAmount())
                .currency(request.getCurrency())
                .durationDays(request.getDurationDays())
                .benefitDescription(request.getBenefitDescription())
                .visible(request.getVisible())
                .displayOrder(request.getDisplayOrder())
                .build();
    }

    public void update(SubscriptionPlanUpdateRequest request) {
        if (request.getCode() != null) {
            this.code = request.getCode();
        }
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription();
        }
        if (request.getPriceAmount() != null) {
            this.priceAmount = request.getPriceAmount();
        }
        if (request.getCurrency() != null) {
            this.currency = request.getCurrency();
        }
        if (request.getDurationDays() != null) {
            this.durationDays = request.getDurationDays();
        }
        if (request.getBenefitDescription() != null) {
            this.benefitDescription = request.getBenefitDescription();
        }
        if (request.getVisible() != null) {
            this.visible = request.getVisible();
        }
        if (request.getDisplayOrder() != null) {
            this.displayOrder = request.getDisplayOrder();
        }
    }

    public void changeVisibility(boolean visible) {
        this.visible = visible;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.visible = false;
    }
}
