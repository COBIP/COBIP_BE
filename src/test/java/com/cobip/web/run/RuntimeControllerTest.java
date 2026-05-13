package com.cobip.web.run;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.run.CodeRunService;
import com.cobip.domain.run.JudgeService;
import com.cobip.dto.run.JudgeResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({RunController.class, JudgeController.class})
@AutoConfigureMockMvc(addFilters = false)
class RuntimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodeRunService codeRunService;

    @MockitoBean
    private JudgeService judgeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void runAcceptsSourceCodeAlias() throws Exception {
        when(codeRunService.runWithInput(eq("JAVASCRIPT"), eq("console.log(3);"), eq("")))
                .thenReturn("3");

        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "language": "JAVASCRIPT",
                              "sourceCode": "console.log(3);"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.output").value("3"));
    }

    @Test
    void judgeAcceptsSourceCodeAlias() throws Exception {
        when(judgeService.judge(eq("JAVASCRIPT"), eq("console.log(3);")))
                .thenReturn(new JudgeResponse(true, "3", "3"));

        mockMvc.perform(post("/api/judge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "language": "JAVASCRIPT",
                              "sourceCode": "console.log(3);"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void judgeRejectsMissingCodeAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/judge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "language": "PYTHON"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
