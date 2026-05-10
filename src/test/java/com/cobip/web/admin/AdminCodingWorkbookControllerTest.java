package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.cobip.domain.coding.AdminCodingWorkbookService;
import com.cobip.domain.coding.CodingDifficulty;
import com.cobip.domain.coding.CodingWorkbookStatus;
import com.cobip.dto.admin.AdminCodingProblemCreateRequest;
import com.cobip.dto.admin.AdminCodingProblemUpdateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookCreateRequest;
import com.cobip.dto.admin.AdminCodingWorkbookUpdateRequest;
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

@WebMvcTest(AdminCodingWorkbookController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCodingWorkbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCodingWorkbookService adminCodingWorkbookService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getWorkbooksReturnsPagedResponse() throws Exception {
        when(adminCodingWorkbookService.getWorkbooks(
                eq("array"),
                eq("algorithm"),
                eq(CodingDifficulty.EASY),
                eq(CodingWorkbookStatus.PUBLISHED),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/coding-workbooks")
                        .param("keyword", "array")
                        .param("category", "algorithm")
                        .param("difficulty", "EASY")
                        .param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void createWorkbookAcceptsPostRequest() throws Exception {
        when(adminCodingWorkbookService.createWorkbook(any(AdminCodingWorkbookCreateRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/coding-workbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "slug": "basic-array",
                              "title": "Basic Array",
                              "category": "algorithm",
                              "difficulty": "EASY",
                              "summary": "Array basics",
                              "description": "Practice arrays.",
                              "status": "DRAFT",
                              "displayOrder": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void workbookCrudEndpointsReturnSuccessEnvelope() throws Exception {
        when(adminCodingWorkbookService.getWorkbook(1L)).thenReturn(null);
        when(adminCodingWorkbookService.updateWorkbook(eq(1L), any(AdminCodingWorkbookUpdateRequest.class)))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/coding-workbooks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(patch("/api/v1/admin/coding-workbooks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Updated Array"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(delete("/api/v1/admin/coding-workbooks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void problemCrudEndpointsReturnSuccessEnvelope() throws Exception {
        when(adminCodingWorkbookService.getProblems(1L)).thenReturn(List.of());
        when(adminCodingWorkbookService.createProblem(eq(1L), any(AdminCodingProblemCreateRequest.class)))
                .thenReturn(null);
        when(adminCodingWorkbookService.getProblem(1L, 10L)).thenReturn(null);
        when(adminCodingWorkbookService.updateProblem(eq(1L), eq(10L), any(AdminCodingProblemUpdateRequest.class)))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/coding-workbooks/1/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(post("/api/v1/admin/coding-workbooks/1/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Two Sum",
                              "category": "algorithm",
                              "difficulty": "EASY",
                              "contentJson": {"type": "doc", "content": []},
                              "explanationJson": {"type": "doc", "content": []},
                              "orderIndex": 1,
                              "timeLimitMillis": 2000,
                              "memoryLimitMb": 256,
                              "status": "DRAFT",
                              "testCases": [
                                {
                                  "input": "1 1",
                                  "expectedOutput": "2",
                                  "sample": true,
                                  "orderIndex": 1
                                }
                              ],
                              "starterCodes": [
                                {
                                  "language": "PYTHON",
                                  "code": "print('hello')"
                                }
                              ]
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/v1/admin/coding-workbooks/1/problems/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(patch("/api/v1/admin/coding-workbooks/1/problems/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Updated Two Sum"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(delete("/api/v1/admin/coding-workbooks/1/problems/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
