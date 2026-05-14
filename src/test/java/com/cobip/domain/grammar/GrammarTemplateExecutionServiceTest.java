package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GrammarTemplateExecutionServiceTest {

    @Mock
    private GrammarTemplateRepository grammarTemplateRepository;

    @Mock
    private GrammarTemplateChapterRepository grammarTemplateChapterRepository;

    @Mock
    private CodeExecutionClient codeExecutionClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GrammarTemplateExecutionService grammarTemplateExecutionService;

    @BeforeEach
    void setUp() {
        grammarTemplateExecutionService = new GrammarTemplateExecutionService(
                grammarTemplateRepository,
                grammarTemplateChapterRepository,
                codeExecutionClient
        );
    }

    @Test
    void runChapterExecutesCodeForPublishedTemplateChapter() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateCodeRunRequest request = runRequest();
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(codeExecutionClient.execute(
                eq(CodingLanguage.PYTHON),
                eq("print(10)"),
                eq(""),
                eq(null),
                eq(5000),
                eq(128)
        )).thenReturn(result());

        var response = grammarTemplateExecutionService.runChapter(1L, 10L, request);

        assertThat(response.getStatus()).isEqualTo(CodingSubmissionStatus.ACCEPTED);
        assertThat(response.getStdout()).isEqualTo("10");
    }

    @Test
    void getExecutionFlowReturnsStaticLineSteps() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateExecutionFlowRequest request = flowRequest();
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));

        var response = grammarTemplateExecutionService.getExecutionFlow(1L, 10L, request);

        assertThat(response.getTraceMode()).isEqualTo("STATIC_LINE_SEQUENCE");
        assertThat(response.getSteps()).hasSize(2);
        assertThat(response.getSteps().getFirst().getLineNumber()).isEqualTo(1);
        assertThat(response.getSteps().getFirst().getEventType()).isEqualTo("ASSIGNMENT");
        assertThat(response.getSteps().getFirst().getActiveVariable().getName()).isEqualTo("x");
        assertThat(response.getSteps().getFirst().getActiveVariable().getValue()).isEqualTo("10");
        assertThat(response.getSteps().get(1).getLineNumber()).isEqualTo(2);
        assertThat(response.getSteps().get(1).getEventType()).isEqualTo("OUTPUT");
        assertThat(response.getSteps().get(1).getActiveOutput().getValue()).isEqualTo("10");
        assertThat(response.getSteps().get(1).getVariables()).hasSize(1);
        assertThat(response.getSteps().get(1).getOutputs()).hasSize(1);
    }

    @Test
    void getExecutionFlowInfersUpdatedVariableState() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateExecutionFlowRequest request = new GrammarTemplateExecutionFlowRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "sourceCode", "x = 10\nx = x + 5\nprint(x)");
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));

        var response = grammarTemplateExecutionService.getExecutionFlow(1L, 10L, request);

        assertThat(response.getSteps()).hasSize(3);
        assertThat(response.getSteps().get(1).getActiveVariable().getChangeType()).isEqualTo("UPDATED");
        assertThat(response.getSteps().get(1).getActiveVariable().getValue()).isEqualTo("15");
        assertThat(response.getSteps().get(2).getVariables().getFirst().getValue()).isEqualTo("15");
        assertThat(response.getSteps().get(2).getActiveOutput().getValue()).isEqualTo("15");
    }

    @Test
    void getExecutionFlowInfersJavaStringOutput() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateExecutionFlowRequest request = new GrammarTemplateExecutionFlowRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.JAVA);
        ReflectionTestUtils.setField(
                request,
                "sourceCode",
                "String name = \"COBIP\";\nSystem.out.println(\"Hello, \" + name);"
        );
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));

        var response = grammarTemplateExecutionService.getExecutionFlow(1L, 10L, request);

        assertThat(response.getSteps()).hasSize(2);
        assertThat(response.getSteps().getFirst().getActiveVariable().getDataType()).isEqualTo("String");
        assertThat(response.getSteps().get(1).getActiveOutput().getValue()).isEqualTo("Hello, COBIP");
    }

    @Test
    void runChapterRejectsUnpublishedTemplate() {
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarTemplateExecutionService.runChapter(1L, 10L, runRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND);
    }

    private GrammarTemplateCodeRunRequest runRequest() {
        GrammarTemplateCodeRunRequest request = new GrammarTemplateCodeRunRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "sourceCode", "print(10)");
        ReflectionTestUtils.setField(request, "input", null);
        return request;
    }

    private GrammarTemplateExecutionFlowRequest flowRequest() {
        GrammarTemplateExecutionFlowRequest request = new GrammarTemplateExecutionFlowRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.PYTHON);
        ReflectionTestUtils.setField(request, "sourceCode", "x = 10\nprint(x)");
        return request;
    }

    private CodeExecutionResult result() {
        return new CodeExecutionResult(CodingSubmissionStatus.ACCEPTED, "token", "10", null, null, null, "0.01", 1024);
    }

    private GrammarTemplate template() {
        return GrammarTemplate.builder()
                .id(1L)
                .slug("python-variable")
                .title("Python Variable")
                .language(GrammarTemplateLanguage.PYTHON)
                .category("basic-syntax")
                .difficulty(GrammarTemplateDifficulty.BEGINNER)
                .summary("Python variable basics")
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("python variable")
                .status(GrammarTemplateStatus.PUBLISHED)
                .build();
    }

    private GrammarTemplateChapter chapter(GrammarTemplate template) {
        return GrammarTemplateChapter.builder()
                .id(10L)
                .template(template)
                .title("Variables")
                .orderIndex(1)
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("variables")
                .build();
    }
}
