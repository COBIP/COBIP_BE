package com.cobip.web.coding;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.coding.CodingProblemService;
import com.cobip.domain.coding.CodingSubmissionService;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CodingProblemController.class)
@AutoConfigureMockMvc(addFilters = false)
class CodingProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodingProblemService codingProblemService;

    @MockitoBean
    private CodingSubmissionService codingSubmissionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getProblemReturnsDetailResponseEnvelope() throws Exception {
        when(codingProblemService.getProblem(10L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/coding-problems/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void runProblemReturnsExecutionResponseEnvelope() throws Exception {
        when(codingSubmissionService.runProblem(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/coding-problems/10/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "language": "PYTHON",
                          "sourceCode": "print('hello')",
                          "input": ""
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void submitProblemReturnsSubmissionResponseEnvelope() throws Exception {
        when(codingSubmissionService.submitProblem(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/coding-problems/10/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "language": "PYTHON",
                          "sourceCode": "print('hello')"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
