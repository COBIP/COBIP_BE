package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.report.AdminReportService;
import com.cobip.domain.report.ReportStatus;
import com.cobip.domain.report.ReportTargetType;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminReportStatusUpdateRequest;
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

@WebMvcTest(AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReportService adminReportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getReportsReturnsPagedResponse() throws Exception {
        when(adminReportService.getReports(
                eq(ReportStatus.PENDING),
                eq(ReportTargetType.TEMPLATE),
                eq(2L),
                eq(10L),
                eq("spam"),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/reports")
                        .param("status", "PENDING")
                        .param("targetType", "TEMPLATE")
                        .param("reporterId", "2")
                        .param("targetId", "10")
                        .param("keyword", "spam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getReportReturnsDetailResponseEnvelope() throws Exception {
        when(adminReportService.getReport(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatusAcceptsPatchRequest() throws Exception {
        when(adminReportService.updateStatus(
                eq(1L),
                any(AdminReportStatusUpdateRequest.class),
                nullable(User.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/reports/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "RESOLVED",
                              "adminMemo": "Handled"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatusRejectsMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/reports/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "adminMemo": "Handled"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
