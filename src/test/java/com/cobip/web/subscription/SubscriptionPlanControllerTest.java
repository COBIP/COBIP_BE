package com.cobip.web.subscription;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.subscription.SubscriptionPlanService;
import com.cobip.dto.subscription.SubscriptionPlanPublicResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubscriptionPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionPlanService subscriptionPlanService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getPlansReturnsVisiblePagedResponse() throws Exception {
        when(subscriptionPlanService.getVisiblePlans(eq("pro"), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.<SubscriptionPlanPublicResponse>empty()));

        mockMvc.perform(get("/api/v1/subscription-plans")
                        .param("keyword", "pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getPlanReturnsVisiblePlanResponseEnvelope() throws Exception {
        when(subscriptionPlanService.getVisiblePlan(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/subscription-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
