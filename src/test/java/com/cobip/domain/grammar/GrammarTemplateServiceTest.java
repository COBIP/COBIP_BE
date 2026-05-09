package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.aws.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private GrammarTemplateTextExtractor textExtractor;

    @Mock
    private S3Service s3Service;

    private GrammarTemplateService grammarTemplateService;

    @BeforeEach
    void setUp() {
        grammarTemplateService = new GrammarTemplateService(
                grammarTemplateRepository,
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
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));

        var response = grammarTemplateService.getPublishedGrammarTemplate(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContentJson()).isNotNull();
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
}
