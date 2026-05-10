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

import java.util.List;

import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateMediaType;
import com.cobip.domain.grammar.GrammarTemplatePracticeFile;
import com.cobip.domain.grammar.GrammarTemplatePracticeFileType;
import com.cobip.domain.grammar.GrammarTemplateService;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.dto.grammar.GrammarTemplateChapterCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateChapterResponse;
import com.cobip.dto.grammar.GrammarTemplateChapterUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileRequest;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileResponse;
import com.cobip.dto.grammar.GrammarTemplateUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void getChaptersReturnsChapterList() throws Exception {
        when(grammarTemplateService.getChapters(1L)).thenReturn(List.of(chapterResponse()));

        mockMvc.perform(get("/api/v1/admin/grammar-templates/1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L))
                .andExpect(jsonPath("$.data[0].title").value("Variables"));
    }

    @Test
    void createChapterAcceptsRequestBody() throws Exception {
        when(grammarTemplateService.createChapter(
                eq(1L),
                any(GrammarTemplateChapterCreateRequest.class)
        )).thenReturn(chapterResponse());

        mockMvc.perform(post("/api/v1/admin/grammar-templates/1/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Variables",
                              "orderIndex": 1,
                              "contentJson": {
                                "type": "doc"
                              }
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Variables"));
    }

    @Test
    void updateChapterAcceptsPatchRequest() throws Exception {
        when(grammarTemplateService.updateChapter(
                eq(1L),
                eq(10L),
                any(GrammarTemplateChapterUpdateRequest.class)
        )).thenReturn(chapterResponse());

        mockMvc.perform(patch("/api/v1/admin/grammar-templates/1/chapters/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Variables Updated",
                              "orderIndex": 2
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteChapterReturnsSuccessResponse() throws Exception {
        doNothing().when(grammarTemplateService).deleteChapter(1L, 10L);

        mockMvc.perform(delete("/api/v1/admin/grammar-templates/1/chapters/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPracticeFilesReturnsFileList() throws Exception {
        when(grammarTemplateService.getPracticeFiles(1L, 10L)).thenReturn(List.of(practiceFileResponse()));

        mockMvc.perform(get("/api/v1/admin/grammar-templates/1/chapters/10/practice-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].filePath").value("src/main.py"))
                .andExpect(jsonPath("$.data[0].nodeType").value("FILE"));
    }

    @Test
    void createPracticeFileAcceptsRequestBody() throws Exception {
        when(grammarTemplateService.createPracticeFile(
                eq(1L),
                eq(10L),
                any(GrammarTemplatePracticeFileRequest.class)
        )).thenReturn(practiceFileResponse());

        mockMvc.perform(post("/api/v1/admin/grammar-templates/1/chapters/10/practice-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nodeType": "FILE",
                              "filePath": "src/main.py",
                              "language": "python",
                              "content": "print(10)",
                              "readOnly": false,
                              "orderIndex": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.filePath").value("src/main.py"));
    }

    @Test
    void updatePracticeFileAcceptsPatchRequest() throws Exception {
        when(grammarTemplateService.updatePracticeFile(
                eq(1L),
                eq(10L),
                eq(100L),
                any(GrammarTemplatePracticeFileRequest.class)
        )).thenReturn(practiceFileResponse());

        mockMvc.perform(patch("/api/v1/admin/grammar-templates/1/chapters/10/practice-files/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nodeType": "FILE",
                              "filePath": "src/main.py",
                              "language": "python",
                              "content": "print(10)",
                              "readOnly": false,
                              "orderIndex": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePracticeFileReturnsSuccessResponse() throws Exception {
        doNothing().when(grammarTemplateService).deletePracticeFile(1L, 10L, 100L);

        mockMvc.perform(delete("/api/v1/admin/grammar-templates/1/chapters/10/practice-files/100"))
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

    private GrammarTemplateChapterResponse chapterResponse() {
        GrammarTemplate template = GrammarTemplate.builder()
                .id(1L)
                .build();
        GrammarTemplateChapter chapter = GrammarTemplateChapter.builder()
                .id(10L)
                .template(template)
                .title("Variables")
                .orderIndex(1)
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("variables")
                .build();
        return GrammarTemplateChapterResponse.from(chapter);
    }

    private GrammarTemplatePracticeFileResponse practiceFileResponse() {
        GrammarTemplate template = GrammarTemplate.builder()
                .id(1L)
                .build();
        GrammarTemplateChapter chapter = GrammarTemplateChapter.builder()
                .id(10L)
                .template(template)
                .title("Variables")
                .orderIndex(1)
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("variables")
                .build();
        GrammarTemplatePracticeFile file = GrammarTemplatePracticeFile.builder()
                .id(100L)
                .template(template)
                .chapter(chapter)
                .nodeType(GrammarTemplatePracticeFileType.FILE)
                .filePath("src/main.py")
                .language("python")
                .content("print(10)")
                .readOnly(false)
                .orderIndex(1)
                .build();
        return GrammarTemplatePracticeFileResponse.from(file);
    }
}
