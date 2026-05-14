package com.cobip.web.grammar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateExecutionService;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.dto.grammar.GrammarTemplatePublicSummaryResponse;
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

@WebMvcTest(GrammarTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class GrammarTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GrammarTemplateService grammarTemplateService;

    @MockitoBean
    private GrammarTemplateExecutionService grammarTemplateExecutionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getCategoriesReturnsCategoryList() throws Exception {
        when(grammarTemplateService.getPublishedCategories(GrammarTemplateLanguage.JAVA))
                .thenReturn(List.of("basic-syntax", "condition"));

        mockMvc.perform(get("/api/v1/grammar-templates/categories")
                        .param("language", "JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("basic-syntax"));
    }

    @Test
    void getGrammarTemplatesReturnsPublishedPagedResponse() throws Exception {
        when(grammarTemplateService.getPublishedGrammarTemplates(
                eq("variable"),
                eq(GrammarTemplateLanguage.JAVA),
                eq("basic-syntax"),
                eq(GrammarTemplateDifficulty.BEGINNER),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.<GrammarTemplatePublicSummaryResponse>empty()));

        mockMvc.perform(get("/api/v1/grammar-templates")
                        .param("keyword", "variable")
                        .param("language", "JAVA")
                        .param("category", "basic-syntax")
                        .param("difficulty", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getGrammarTemplateReturnsDetailResponseEnvelope() throws Exception {
        when(grammarTemplateService.getPublishedGrammarTemplate(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/grammar-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void runChapterAcceptsCodeRunRequest() throws Exception {
        when(grammarTemplateExecutionService.runChapter(
                any(),
                eq(1L),
                eq(10L),
                any(GrammarTemplateCodeRunRequest.class)
        )).thenReturn(null);

        mockMvc.perform(post("/api/v1/grammar-templates/1/chapters/10/run")
                        .contentType("application/json")
                        .content("""
                            {
                              "language": "PYTHON",
                              "sourceCode": "print(10)",
                              "input": ""
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void executionFlowAcceptsSourceCodeRequest() throws Exception {
        when(grammarTemplateExecutionService.getExecutionFlow(
                any(),
                eq(1L),
                eq(10L),
                any(GrammarTemplateExecutionFlowRequest.class)
        )).thenReturn(null);

        mockMvc.perform(post("/api/v1/grammar-templates/1/chapters/10/execution-flow")
                        .contentType("application/json")
                        .content("""
                            {
                              "language": "PYTHON",
                              "sourceCode": "x = 10\\nprint(x)"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
