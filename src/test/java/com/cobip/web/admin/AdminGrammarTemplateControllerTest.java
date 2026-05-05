package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateMediaType;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplateUpdateRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(AdminGrammarTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminGrammarTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GrammarTemplateService grammarTemplateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createGrammarTemplateAcceptsRequestBody() throws Exception {
        when(grammarTemplateService.createGrammarTemplate(any(GrammarTemplateCreateRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/grammar-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "slug": "java-variable-declaration",
                              "title": "Java 변수 선언",
                              "language": "JAVA",
                              "category": "basic-syntax",
                              "difficulty": "BEGINNER",
                              "summary": "Java 변수 선언 문법을 학습합니다.",
                              "contentJson": {
                                "sections": [
                                  {
                                    "heading": "변수 선언",
                                    "body": "타입과 이름을 작성합니다."
                                  }
                                ]
                              },
                              "status": "DRAFT"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getGrammarTemplatesReturnsPagedResponse() throws Exception {
        when(grammarTemplateService.getGrammarTemplates(
                eq("variable"),
                eq(GrammarTemplateLanguage.JAVA),
                eq("basic-syntax"),
                eq(GrammarTemplateDifficulty.BEGINNER),
                eq(GrammarTemplateStatus.DRAFT),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/grammar-templates")
                        .param("keyword", "variable")
                        .param("language", "JAVA")
                        .param("category", "basic-syntax")
                        .param("difficulty", "BEGINNER")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getGrammarTemplateReturnsDetailResponseEnvelope() throws Exception {
        when(grammarTemplateService.getGrammarTemplate(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/grammar-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateGrammarTemplateAcceptsPatchRequest() throws Exception {
        when(grammarTemplateService.updateGrammarTemplate(eq(1L), any(GrammarTemplateUpdateRequest.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/grammar-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Java 변수 선언 수정",
                              "summary": "수정된 요약입니다."
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteGrammarTemplateReturnsSuccessResponse() throws Exception {
        doNothing().when(grammarTemplateService).deleteGrammarTemplate(1L);

        mockMvc.perform(delete("/api/v1/admin/grammar-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeStatusAcceptsPatchRequest() throws Exception {
        when(grammarTemplateService.changeStatus(1L, GrammarTemplateStatus.PUBLISHED)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/grammar-templates/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "PUBLISHED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void uploadMediaAcceptsImageFile() throws Exception {
        when(grammarTemplateService.uploadMedia(
                eq(1L),
                eq(GrammarTemplateMediaType.IMAGE),
                any(MultipartFile.class)
        )).thenReturn(new GrammarTemplateMediaUploadResponse(
                1L,
                GrammarTemplateMediaType.IMAGE,
                "grammar-templates/1/images/java.png",
                "https://example.com/grammar-templates/1/images/java.png",
                "image/png"
        ));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "java.png",
                "image/png",
                "image".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/grammar-templates/1/media")
                        .file(file)
                        .param("type", "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("IMAGE"))
                .andExpect(jsonPath("$.data.fileUrl").value("https://example.com/grammar-templates/1/images/java.png"));
    }

    @Test
    void uploadMediaAcceptsVideoFile() throws Exception {
        when(grammarTemplateService.uploadMedia(
                eq(1L),
                eq(GrammarTemplateMediaType.VIDEO),
                any(MultipartFile.class)
        )).thenReturn(new GrammarTemplateMediaUploadResponse(
                1L,
                GrammarTemplateMediaType.VIDEO,
                "grammar-templates/1/videos/java.mp4",
                "https://example.com/grammar-templates/1/videos/java.mp4",
                "video/mp4"
        ));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "java.mp4",
                "video/mp4",
                "video".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/grammar-templates/1/media")
                        .file(file)
                        .param("type", "VIDEO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("VIDEO"))
                .andExpect(jsonPath("$.data.fileUrl").value("https://example.com/grammar-templates/1/videos/java.mp4"));
    }

    @Test
    void createGrammarTemplateRejectsMissingContentJson() throws Exception {
        mockMvc.perform(post("/api/v1/admin/grammar-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "slug": "java-variable-declaration",
                              "title": "Java 변수 선언",
                              "language": "JAVA",
                              "category": "basic-syntax",
                              "difficulty": "BEGINNER",
                              "summary": "Java 변수 선언 문법을 학습합니다."
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
