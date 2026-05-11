package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.template.AdminTemplateService;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminTemplateCreateRequest;
import com.cobip.dto.admin.AdminTemplateExposureUpdateRequest;
import com.cobip.dto.admin.AdminTemplateUpdateRequest;
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

@WebMvcTest(AdminTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTemplateService adminTemplateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getTemplatesReturnsPagedResponse() throws Exception {
        when(adminTemplateService.getTemplates(
                eq("spring"),
                eq("backend"),
                eq(TemplateDifficulty.BEGINNER),
                eq(TemplateVisibility.PUBLIC),
                eq(TemplateAccessLevel.FREE),
                eq(1L),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/templates")
                        .param("keyword", "spring")
                        .param("category", "backend")
                        .param("difficulty", "BEGINNER")
                        .param("visibility", "PUBLIC")
                        .param("accessLevel", "FREE")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getTemplateReturnsDetailResponseEnvelope() throws Exception {
        when(adminTemplateService.getTemplate(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createTemplateAcceptsPostRequest() throws Exception {
        when(adminTemplateService.createTemplate(any(AdminTemplateCreateRequest.class), isNull(User.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "JWT Login",
                              "description": "Build JWT login.",
                              "summary": "JWT login summary",
                              "category": "backend",
                              "difficulty": "BEGINNER",
                              "techStacks": ["Spring"],
                              "tags": ["auth"],
                              "runtime": "java17",
                              "previewImage": "https://cdn.example.com/preview.png",
                              "license": "MIT",
                              "source": "internal",
                              "published": true,
                              "accessLevel": "FREE",
                              "interviewQuestions": [
                                {
                                  "question": "JWT를 사용하는 이유는?",
                                  "answerHint": "상태 비저장 인증"
                                }
                              ],
                              "testCases": [
                                {
                                  "input": "1 2",
                                  "expected_output": "3",
                                  "description": "기본 케이스"
                                }
                              ]
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateTemplateAcceptsPatchRequest() throws Exception {
        when(adminTemplateService.updateTemplate(eq(1L), any(AdminTemplateUpdateRequest.class), isNull(User.class)))
                .thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Updated JWT Login"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateExposureAcceptsPatchRequest() throws Exception {
        when(adminTemplateService.updateExposure(
                eq(1L),
                any(AdminTemplateExposureUpdateRequest.class),
                isNull(User.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/templates/1/exposure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "visibility": "PRIVATE",
                              "accessLevel": "PREMIUM"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateExposureRejectsMissingAccessLevel() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/templates/1/exposure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "visibility": "PRIVATE"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteTemplateAcceptsDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
