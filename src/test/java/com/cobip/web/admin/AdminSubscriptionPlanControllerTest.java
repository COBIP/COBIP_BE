package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.subscription.SubscriptionPlanService;
import com.cobip.dto.admin.SubscriptionPlanCreateRequest;
import com.cobip.dto.admin.SubscriptionPlanUpdateRequest;
import com.cobip.dto.admin.SubscriptionPlanVisibilityUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminSubscriptionPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionPlanService subscriptionPlanService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createPlanAcceptsRequestBody() throws Exception {
        when(subscriptionPlanService.createPlan(any(SubscriptionPlanCreateRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/subscription-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "code": "PRO_MONTHLY",
                              "name": "Pro Monthly",
                              "description": "Monthly pro plan",
                              "priceAmount": 9900,
                              "currency": "KRW",
                              "durationDays": 30,
                              "benefitDescription": "Premium templates",
                              "visible": true,
                              "displayOrder": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPlansReturnsPagedResponse() throws Exception {
        when(subscriptionPlanService.getPlans(eq("pro"), eq(true), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/subscription-plans")
                        .param("keyword", "pro")
                        .param("visible", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getPlanReturnsDetailResponseEnvelope() throws Exception {
        when(subscriptionPlanService.getPlan(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/subscription-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updatePlanAcceptsPatchRequest() throws Exception {
        when(subscriptionPlanService.updatePlan(eq(1L), any(SubscriptionPlanUpdateRequest.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/subscription-plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Pro Annual",
                              "priceAmount": 99000,
                              "durationDays": 365
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeVisibilityAcceptsPatchRequest() throws Exception {
        when(subscriptionPlanService.changeVisibility(
                eq(1L),
                any(SubscriptionPlanVisibilityUpdateRequest.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/subscription-plans/1/visibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "visible": false
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePlanReturnsSuccessResponse() throws Exception {
        doNothing().when(subscriptionPlanService).deletePlan(1L);

        mockMvc.perform(delete("/api/v1/admin/subscription-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createPlanRejectsMissingCode() throws Exception {
        mockMvc.perform(post("/api/v1/admin/subscription-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Pro Monthly",
                              "description": "Monthly pro plan",
                              "priceAmount": 9900,
                              "currency": "KRW",
                              "durationDays": 30,
                              "benefitDescription": "Premium templates",
                              "visible": true,
                              "displayOrder": 1
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
