package com.cobip.web.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.admin.AdminStatisticsService;
import com.cobip.dto.admin.AdminOperationStatisticsResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminStatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminStatisticsService adminStatisticsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getOverviewReturnsOperationStatistics() throws Exception {
        when(adminStatisticsService.getOverview()).thenReturn(new AdminOperationStatisticsResponse(
                10L,
                3L,
                7L,
                5L,
                2L,
                4L,
                3L,
                8L,
                6L,
                40L,
                35L,
                1200L,
                9L,
                11L
        ));

        mockMvc.perform(get("/api/v1/admin/statistics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userCount").value(10))
                .andExpect(jsonPath("$.data.activeSubscriptionCount").value(3))
                .andExpect(jsonPath("$.data.totalStudySeconds").value(1200));
    }
}
