package com.cobip.web.coding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookService;
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
        when(codingWorkbookService.getWorkbook(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/coding-workbooks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
