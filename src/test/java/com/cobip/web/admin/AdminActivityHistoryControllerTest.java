package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.activity.AdminActivityHistoryService;
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

@WebMvcTest(AdminActivityHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminActivityHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminActivityHistoryService adminActivityHistoryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getActivityHistoriesReturnsPagedResponse() throws Exception {
        when(adminActivityHistoryService.getActivityHistories(
                eq(1L),
                eq(ActivityType.ADMIN_USER_STATUS_CHANGED),
                eq("USER"),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/activity-histories")
                        .param("userId", "1")
                        .param("type", "ADMIN_USER_STATUS_CHANGED")
                        .param("targetType", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
