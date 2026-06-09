package com.cobip.web.coding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.coding.CodingWorkbookDetailResponse;
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

@WebMvcTest(CodingWorkbookController.class)
@AutoConfigureMockMvc(addFilters = false)
class CodingWorkbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodingWorkbookService codingWorkbookService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getWorkbooksReturnsPagedResponse() throws Exception {
        when(codingWorkbookService.getWorkbooks(
                eq("array"),
                eq("algorithm"),
                eq(CodingDifficulty.EASY),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/coding-workbooks")
                        .param("keyword", "array")
                        .param("category", "algorithm")
                        .param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getWorkbookReturnsDetailResponseEnvelope() throws Exception {
        when(codingWorkbookService.getWorkbook(any(), eq(1L))).thenReturn(detailResponse());

        mockMvc.perform(get("/api/v1/coding-workbooks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.problems[0].solved").value(true));
    }

    private CodingWorkbookDetailResponse detailResponse() {
        var workbook = com.cobip.domain.coding.CodingWorkbook.builder()
                .id(1L)
                .slug("basic-array")
                .title("Basic Array")
                .category("algorithm")
                .difficulty(CodingDifficulty.EASY)
                .summary("summary")
                .description("description")
                .status(com.cobip.domain.coding.CodingWorkbookStatus.PUBLISHED)
                .displayOrder(1)
                .build();

        var problem = com.cobip.domain.coding.CodingProblem.builder()
                .id(10L)
                .workbook(workbook)
                .title("Two Sum")
                .category("array")
                .difficulty(CodingDifficulty.EASY)
                .orderIndex(1)
                .timeLimitMillis(2000)
                .memoryLimitMb(256)
                .status(com.cobip.domain.coding.CodingProblemStatus.PUBLISHED)
                .build();

        return CodingWorkbookDetailResponse.of(workbook, java.util.List.of(problem), java.util.Set.of(10L));
    }
}
