package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.dto.grammar.GrammarTemplateChapterCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateChapterUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.aws.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class GrammarTemplateServiceTest {

    @Mock
    private GrammarTemplateRepository grammarTemplateRepository;

    @Mock
    private GrammarTemplateChapterRepository grammarTemplateChapterRepository;

    @Mock
    private GrammarTemplatePracticeFileRepository grammarTemplatePracticeFileRepository;

    @Mock
    private GrammarTemplateTextExtractor textExtractor;

    @Mock
    private S3Service s3Service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GrammarTemplateService grammarTemplateService;

    @BeforeEach
    void setUp() {
        grammarTemplateService = new GrammarTemplateService(
                grammarTemplateRepository,
                grammarTemplateChapterRepository,
                grammarTemplatePracticeFileRepository,
                textExtractor,
                s3Service
        );
    }

    @Test
    void getPublishedCategoriesReturnsPublishedCategoryNames() {
        when(grammarTemplateRepository.findDistinctCategories(
                GrammarTemplateStatus.PUBLISHED,
                GrammarTemplateLanguage.JAVA
        )).thenReturn(List.of("basic-syntax", "loop"));

        List<String> categories = grammarTemplateService.getPublishedCategories(GrammarTemplateLanguage.JAVA);

        assertThat(categories).containsExactly("basic-syntax", "loop");
    }

    @Test
    void getPublishedGrammarTemplatesReturnsPublicSummaries() {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.PUBLISHED);
        when(grammarTemplateRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(template)));

        var response = grammarTemplateService.getPublishedGrammarTemplates(
                "variable",
                GrammarTemplateLanguage.JAVA,
                "basic-syntax",
                GrammarTemplateDifficulty.BEGINNER,
                PageRequest.of(0, 20)
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void getPublishedGrammarTemplateReturnsOnlyPublishedDetail() {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.PUBLISHED);
        GrammarTemplateChapter chapter = chapter(10L, template, "Variables", 1);
        GrammarTemplatePracticeFile file = practiceFile(100L, template, chapter, "src/main.py");
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByTemplateIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(1L))
                .thenReturn(List.of(chapter));
        when(grammarTemplatePracticeFileRepository.findByTemplateIdOrderByChapterIdAscOrderIndexAscIdAsc(1L))
                .thenReturn(List.of(file));

        var response = grammarTemplateService.getPublishedGrammarTemplate(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContentJson()).isNotNull();
        assertThat(response.getChapters()).hasSize(1);
        assertThat(response.getChapters().getFirst().getTitle()).isEqualTo("Variables");
        assertThat(response.getChapters().getFirst().getPracticeFiles()).hasSize(1);
        assertThat(response.getChapters().getFirst().getPracticeFiles().getFirst().getFilePath())
                .isEqualTo("src/main.py");
    }

    @Test
    void getPublishedGrammarTemplateRejectsMissingOrUnpublishedTemplate() {
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarTemplateService.getPublishedGrammarTemplate(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND);
    }

    @Test
    void createChapterStoresSearchableContent() throws Exception {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.DRAFT);
        GrammarTemplateChapterCreateRequest request = chapterCreateRequest();
        when(grammarTemplateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(textExtractor.extract(request.getContentJson())).thenReturn("variables intro");
        when(grammarTemplateChapterRepository.save(any(GrammarTemplateChapter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = grammarTemplateService.createChapter(1L, request);

        ArgumentCaptor<GrammarTemplateChapter> chapterCaptor = ArgumentCaptor.forClass(GrammarTemplateChapter.class);
        verify(grammarTemplateChapterRepository).save(chapterCaptor.capture());
        assertThat(response.getTemplateId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Variables");
        assertThat(chapterCaptor.getValue().getSearchableText()).isEqualTo("variables intro");
    }

    @Test
    void updateChapterChangesTitleOrderAndContent() throws Exception {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.DRAFT);
        GrammarTemplateChapter chapter = chapter(10L, template, "Variables", 1);
        GrammarTemplateChapterUpdateRequest request = chapterUpdateRequest();
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(textExtractor.extract(request.getContentJson())).thenReturn("updated variables");

        var response = grammarTemplateService.updateChapter(1L, 10L, request);

        assertThat(response.getTitle()).isEqualTo("Variables Updated");
        assertThat(response.getOrderIndex()).isEqualTo(2);
        assertThat(chapter.getSearchableText()).isEqualTo("updated variables");
    }

    @Test
    void updateChapterRejectsMissingChapter() {
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarTemplateService.updateChapter(
                1L,
                10L,
                new GrammarTemplateChapterUpdateRequest()
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAMMAR_TEMPLATE_CHAPTER_NOT_FOUND);
    }

    @Test
    void createPracticeFileSavesChapterFile() throws Exception {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.DRAFT);
        GrammarTemplateChapter chapter = chapter(10L, template, "Variables", 1);
        GrammarTemplatePracticeFileRequest request = practiceFileRequest("src/main.py");
        when(grammarTemplateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(grammarTemplatePracticeFileRepository.save(any(GrammarTemplatePracticeFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = grammarTemplateService.createPracticeFile(1L, 10L, request);

        ArgumentCaptor<GrammarTemplatePracticeFile> fileCaptor =
                ArgumentCaptor.forClass(GrammarTemplatePracticeFile.class);
        verify(grammarTemplatePracticeFileRepository).save(fileCaptor.capture());
        assertThat(response.getTemplateId()).isEqualTo(1L);
        assertThat(response.getChapterId()).isEqualTo(10L);
        assertThat(response.getNodeType()).isEqualTo(GrammarTemplatePracticeFileType.FILE);
        assertThat(fileCaptor.getValue().getFilePath()).isEqualTo("src/main.py");
    }

    @Test
    void updatePracticeFileChangesFileContent() throws Exception {
        GrammarTemplate template = template(1L, GrammarTemplateStatus.DRAFT);
        GrammarTemplateChapter chapter = chapter(10L, template, "Variables", 1);
        GrammarTemplatePracticeFile file = practiceFile(100L, template, chapter, "src/main.py");
        GrammarTemplatePracticeFileRequest request = practiceFileRequest("src/app.py");
        when(grammarTemplateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(grammarTemplatePracticeFileRepository.findByIdAndTemplateIdAndChapterId(100L, 1L, 10L))
                .thenReturn(Optional.of(file));

        var response = grammarTemplateService.updatePracticeFile(1L, 10L, 100L, request);

        assertThat(response.getFilePath()).isEqualTo("src/app.py");
        assertThat(file.getContent()).isEqualTo("print(10)");
    }

    private GrammarTemplate template(Long id, GrammarTemplateStatus status) {
        return GrammarTemplate.builder()
                .id(id)
                .slug("java-variable")
                .title("Java Variable")
                .language(GrammarTemplateLanguage.JAVA)
                .category("basic-syntax")
                .difficulty(GrammarTemplateDifficulty.BEGINNER)
                .summary("Java variable basics")
                .contentJson(new ObjectMapper().createObjectNode().put("type", "doc"))
                .searchableText("java variable")
                .status(status)
                .build();
    }

    private GrammarTemplateChapter chapter(Long id, GrammarTemplate template, String title, int orderIndex) {
        return GrammarTemplateChapter.builder()
                .id(id)
                .template(template)
                .title(title)
                .orderIndex(orderIndex)
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("variables")
                .build();
    }

    private GrammarTemplatePracticeFile practiceFile(
        Long id,
        GrammarTemplate template,
        GrammarTemplateChapter chapter,
        String filePath
    ) {
        return GrammarTemplatePracticeFile.builder()
                .id(id)
                .template(template)
                .chapter(chapter)
                .nodeType(GrammarTemplatePracticeFileType.FILE)
                .filePath(filePath)
                .language("python")
                .content("print(10)")
                .readOnly(false)
                .orderIndex(1)
                .build();
    }

    private GrammarTemplateChapterCreateRequest chapterCreateRequest() throws Exception {
        return objectMapper.readValue("""
            {
              "title": "Variables",
              "orderIndex": 1,
              "contentJson": {
                "type": "doc",
                "content": [
                  {
                    "type": "paragraph",
                    "content": [
                      {
                        "type": "text",
                        "text": "Variables store values."
                      }
                    ]
                  }
                ]
              }
            }
            """, GrammarTemplateChapterCreateRequest.class);
    }

    private GrammarTemplateChapterUpdateRequest chapterUpdateRequest() throws Exception {
        return objectMapper.readValue("""
            {
              "title": "Variables Updated",
              "orderIndex": 2,
              "contentJson": {
                "type": "doc",
                "content": [
                  {
                    "type": "paragraph",
                    "content": [
                      {
                        "type": "text",
                        "text": "Updated variables content."
                      }
                    ]
                  }
                ]
              }
            }
            """, GrammarTemplateChapterUpdateRequest.class);
    }

    private GrammarTemplatePracticeFileRequest practiceFileRequest(String filePath) throws Exception {
        return objectMapper.readValue("""
            {
              "nodeType": "FILE",
              "filePath": "%s",
              "language": "python",
              "content": "print(10)",
              "readOnly": false,
              "orderIndex": 1
            }
            """.formatted(filePath), GrammarTemplatePracticeFileRequest.class);
    }
}
