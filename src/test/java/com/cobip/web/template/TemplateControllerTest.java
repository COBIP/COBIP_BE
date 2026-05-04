package com.cobip.web.template;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.certificate.CertificateService;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateService;
import com.cobip.domain.user.User;
import com.cobip.dto.template.TemplateCreateRequest;
import com.cobip.dto.template.TemplateFileUploadResponse;
import com.cobip.dto.template.TemplateUpdateRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemplateService templateService;

    @MockitoBean
    private CertificateService certificateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getTemplatesReturnsPagedResponse() throws Exception {
        when(templateService.getTemplates(eq("spring"), eq("backend"), eq(TemplateDifficulty.BEGINNER), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/templates")
                        .param("keyword", "spring")
                        .param("category", "backend")
                        .param("difficulty", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getTemplateReturnsDetailResponseEnvelope() throws Exception {
        when(templateService.getTemplate(eq(1L), isNull(User.class))).thenReturn(null);

        mockMvc.perform(get("/api/v1/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createTemplateAcceptsRequestBody() throws Exception {
        when(templateService.createTemplate(isNull(User.class), any(TemplateCreateRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Spring JWT Template",
                              "description": "JWT 기반 인증 템플릿",
                              "category": "backend",
                              "difficulty": "BEGINNER",
                              "techStacks": ["Spring", "JWT"],
                              "visibility": "PUBLIC",
                              "accessLevel": "FREE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateTemplateAcceptsPatchRequest() throws Exception {
        when(templateService.updateTemplate(isNull(User.class), eq(1L), any(TemplateUpdateRequest.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Updated Template",
                              "accessLevel": "PREMIUM"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteTemplateReturnsSuccessResponse() throws Exception {
        doNothing().when(templateService).deleteTemplate(isNull(User.class), eq(1L));

        mockMvc.perform(delete("/api/v1/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void uploadTemplateFileAcceptsMultipartFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "main.zip", "application/zip", "zip".getBytes());
        when(templateService.uploadFile(isNull(User.class), eq(1L), any()))
                .thenReturn(new TemplateFileUploadResponse(1L, "templates/1/main.zip", "https://s3/main.zip"));

        mockMvc.perform(multipart("/api/v1/templates/1/file").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileUrl").value("https://s3/main.zip"));
    }

    @Test
    void uploadThumbnailAcceptsMultipartFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "thumbnail.png", "image/png", "png".getBytes());
        when(templateService.uploadThumbnail(isNull(User.class), eq(1L), any()))
                .thenReturn(new TemplateFileUploadResponse(1L, "templates/1/thumbnail.png", "https://s3/thumbnail.png"));

        mockMvc.perform(multipart("/api/v1/templates/1/thumbnail").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileUrl").value("https://s3/thumbnail.png"));
    }

    @Test
    void favoriteEndpointsReturnSuccessResponse() throws Exception {
        doNothing().when(templateService).addFavorite(isNull(User.class), eq(1L));
        doNothing().when(templateService).removeFavorite(isNull(User.class), eq(1L));

        mockMvc.perform(post("/api/v1/templates/1/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/templates/1/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void issueCertificateReturnsSuccessResponse() throws Exception {
        when(certificateService.issueCertificate(isNull(User.class), eq(1L))).thenReturn(null);

        mockMvc.perform(post("/api/v1/templates/1/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
