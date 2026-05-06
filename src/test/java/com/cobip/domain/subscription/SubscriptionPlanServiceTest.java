package com.cobip.domain.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.dto.admin.SubscriptionPlanCreateRequest;
import com.cobip.dto.admin.SubscriptionPlanUpdateRequest;
import com.cobip.dto.admin.SubscriptionPlanVisibilityUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private SubscriptionPlanService subscriptionPlanService;

    @BeforeEach
    void setUp() {
        subscriptionPlanService = new SubscriptionPlanService(subscriptionPlanRepository);
    }

    @Test
    void createPlanStoresSubscriptionPlan() {
        SubscriptionPlanCreateRequest request = createRequest();
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionPlanService.createPlan(request);

        ArgumentCaptor<SubscriptionPlan> planCaptor = ArgumentCaptor.forClass(SubscriptionPlan.class);
        verify(subscriptionPlanRepository).save(planCaptor.capture());
        assertThat(planCaptor.getValue().getCode()).isEqualTo("PRO_MONTHLY");
        assertThat(planCaptor.getValue().isVisible()).isTrue();
    }

    @Test
    void createPlanRejectsDuplicatedCode() {
        SubscriptionPlanCreateRequest request = createRequest();
        when(subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull("PRO_MONTHLY")).thenReturn(true);

        assertThatThrownBy(() -> subscriptionPlanService.createPlan(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_SUBSCRIPTION_PLAN_CODE);
    }

    @Test
    void updatePlanChangesPlanFields() {
        SubscriptionPlan plan = plan();
        SubscriptionPlanUpdateRequest request = updateRequest();
        when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(plan));

        subscriptionPlanService.updatePlan(1L, request);

        assertThat(plan.getName()).isEqualTo("Pro Annual");
        assertThat(plan.getPriceAmount()).isEqualTo(99000);
        assertThat(plan.getDurationDays()).isEqualTo(365);
    }

    @Test
    void changeVisibilityUpdatesVisibleFlag() {
        SubscriptionPlan plan = plan();
        SubscriptionPlanVisibilityUpdateRequest request = visibilityRequest(false);
        when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(plan));

        subscriptionPlanService.changeVisibility(1L, request);

        assertThat(plan.isVisible()).isFalse();
    }

    @Test
    void deletePlanSoftDeletesAndHidesPlan() {
        SubscriptionPlan plan = plan();
        when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(plan));

        subscriptionPlanService.deletePlan(1L);

        assertThat(plan.getDeletedAt()).isNotNull();
        assertThat(plan.isVisible()).isFalse();
    }

    private SubscriptionPlanCreateRequest createRequest() {
        SubscriptionPlanCreateRequest request = new SubscriptionPlanCreateRequest();
        ReflectionTestUtils.setField(request, "code", "PRO_MONTHLY");
        ReflectionTestUtils.setField(request, "name", "Pro Monthly");
        ReflectionTestUtils.setField(request, "description", "Monthly pro plan");
        ReflectionTestUtils.setField(request, "priceAmount", 9900);
        ReflectionTestUtils.setField(request, "currency", "KRW");
        ReflectionTestUtils.setField(request, "durationDays", 30);
        ReflectionTestUtils.setField(request, "benefitDescription", "Premium templates");
        ReflectionTestUtils.setField(request, "visible", true);
        ReflectionTestUtils.setField(request, "displayOrder", 1);
        return request;
    }

    private SubscriptionPlanUpdateRequest updateRequest() {
        SubscriptionPlanUpdateRequest request = new SubscriptionPlanUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "Pro Annual");
        ReflectionTestUtils.setField(request, "priceAmount", 99000);
        ReflectionTestUtils.setField(request, "durationDays", 365);
        return request;
    }

    private SubscriptionPlanVisibilityUpdateRequest visibilityRequest(boolean visible) {
        SubscriptionPlanVisibilityUpdateRequest request = new SubscriptionPlanVisibilityUpdateRequest();
        ReflectionTestUtils.setField(request, "visible", visible);
        return request;
    }

    private SubscriptionPlan plan() {
        return SubscriptionPlan.builder()
                .id(1L)
                .code("PRO_MONTHLY")
                .name("Pro Monthly")
                .description("Monthly pro plan")
                .priceAmount(9900)
                .currency("KRW")
                .durationDays(30)
                .benefitDescription("Premium templates")
                .visible(true)
                .displayOrder(1)
                .build();
    }
}
