package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.domain.learning.GrammarLearningProgressService;
import com.cobip.domain.practice.ProjectExecutionClient;
import com.cobip.domain.practice.ProjectExecutionResult;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;
import com.cobip.dto.grammar.GrammarTemplateCodeRunRequest;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowRequest;
import com.cobip.dto.grammar.GrammarTemplateMissionSubmissionFileRequest;
import com.cobip.dto.grammar.GrammarTemplateMissionSubmissionRequest;
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
    private GrammarTemplateChapterMissionRepository grammarTemplateChapterMissionRepository;

    @Mock
    private CodeExecutionClient codeExecutionClient;

    @Mock
    private ProjectExecutionClient projectExecutionClient;

    @Mock
    private GrammarLearningProgressService grammarLearningProgressService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GrammarTemplateExecutionService grammarTemplateExecutionService;

    @BeforeEach
    void setUp() {
        grammarTemplateExecutionService = new GrammarTemplateExecutionService(
                grammarTemplateRepository,
                grammarTemplateChapterRepository,
                grammarTemplateChapterMissionRepository,
                codeExecutionClient,
                projectExecutionClient,
                grammarLearningProgressService
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

        var response = grammarTemplateExecutionService.runChapter(null, 1L, 10L, request);

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

        var response = grammarTemplateExecutionService.getExecutionFlow(null, 1L, 10L, request);

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

        var response = grammarTemplateExecutionService.getExecutionFlow(null, 1L, 10L, request);

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

        var response = grammarTemplateExecutionService.getExecutionFlow(null, 1L, 10L, request);

        assertThat(response.getSteps()).hasSize(2);
        assertThat(response.getSteps().getFirst().getActiveVariable().getDataType()).isEqualTo("String");
        assertThat(response.getSteps().get(1).getActiveOutput().getValue()).isEqualTo("Hello, COBIP");
    }

    @Test
    void getExecutionFlowUsesJavaRuntimeTraceWhenAvailable() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateExecutionFlowRequest request = new GrammarTemplateExecutionFlowRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.JAVA);
        ReflectionTestUtils.setField(
                request,
                "sourceCode",
                "public class Main {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        int[] arr = {3, 1, 2};\n"
                        + "        for (int i = 0; i < arr.length; i++) {\n"
                        + "            arr[i] = arr[i] + 1;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}"
        );
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(codeExecutionClient.execute(
                eq(CodingLanguage.JAVA),
                contains("__CobipTrace"),
                eq(""),
                eq(null),
                eq(5000),
                eq(128)
        )).thenReturn(runtimeTraceResult());

        var response = grammarTemplateExecutionService.getExecutionFlow(null, 1L, 10L, request);

        assertThat(response.getTraceMode()).isEqualTo("JAVA_RUNTIME_TRACE");
        assertThat(response.getSteps()).hasSize(4);
        assertThat(response.getSteps().getFirst().getActiveVariable().getElements()).containsExactly("3", "1", "2");
        assertThat(response.getSteps().get(1).getEventType()).isEqualTo("LOOP");
        assertThat(response.getSteps().get(2).getActiveVariable().getName()).isEqualTo("i");
        assertThat(response.getSteps().get(3).getActiveVariable().getActiveIndex()).isZero();
        assertThat(response.getSteps().get(3).getActiveVariable().getElements()).containsExactly("4", "1", "2");
    }

    @Test
    void runChapterRejectsUnpublishedTemplate() {
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> grammarTemplateExecutionService.runChapter(null, 1L, 10L, runRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND);
    }

    @Test
    void submitMissionReturnsAcceptedWhenAnswerMatches() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateChapterMission mission = mission(template, chapter, objectMapper.createObjectNode().put("answer", "int count = 1;"));
        GrammarTemplateMissionSubmissionRequest request = missionSubmissionRequest(
                CodingLanguage.JAVA,
                "src/Main.java",
                "public class Main { void test(){ int count = 1; } }"
        );
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(grammarTemplateChapterMissionRepository.findByIdAndTemplateIdAndChapterId(100L, 1L, 10L))
                .thenReturn(Optional.of(mission));

        var response = grammarTemplateExecutionService.submitMission(null, 1L, 10L, 100L, request);

        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
        assertThat(response.getPassedCount()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
    }

    @Test
    void submitMissionUsesProjectExecutionWhenTestCommandExists() {
        GrammarTemplate template = template();
        GrammarTemplateChapter chapter = chapter(template);
        GrammarTemplateChapterMission mission = mission(
                template,
                chapter,
                objectMapper.createObjectNode()
                        .put("testCommand", "./gradlew test")
                        .put("dockerImage", "gradle:8.14-jdk21")
        );
        GrammarTemplateMissionSubmissionRequest request = missionSubmissionRequest(
                CodingLanguage.JAVA,
                "src/Main.java",
                "public class Main {}"
        );
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(1L, GrammarTemplateStatus.PUBLISHED))
                .thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(chapter));
        when(grammarTemplateChapterMissionRepository.findByIdAndTemplateIdAndChapterId(100L, 1L, 10L))
                .thenReturn(Optional.of(mission));
        when(projectExecutionClient.execute(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ProjectExecutionResult(
                        TemplatePracticeSubmissionStatus.ACCEPTED,
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "Mission accepted.",
                        1000L
                ));

        var response = grammarTemplateExecutionService.submitMission(null, 1L, 10L, 100L, request);

        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
        assertThat(response.getStdout()).isEqualTo("BUILD SUCCESSFUL");
        assertThat(response.getPassedCount()).isEqualTo(1);
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

    private GrammarTemplateMissionSubmissionRequest missionSubmissionRequest(
        CodingLanguage language,
        String filePath,
        String content
    ) {
        GrammarTemplateMissionSubmissionRequest request = new GrammarTemplateMissionSubmissionRequest();
        GrammarTemplateMissionSubmissionFileRequest file = new GrammarTemplateMissionSubmissionFileRequest();
        ReflectionTestUtils.setField(request, "language", language);
        ReflectionTestUtils.setField(file, "filePath", filePath);
        ReflectionTestUtils.setField(file, "content", content);
        ReflectionTestUtils.setField(request, "submittedCode", java.util.List.of(file));
        return request;
    }

    private CodeExecutionResult result() {
        return new CodeExecutionResult(CodingSubmissionStatus.ACCEPTED, "token", "10", null, null, null, "0.01", 1024);
    }

    private CodeExecutionResult runtimeTraceResult() {
        String stdout = String.join("\n",
                JavaRuntimeExecutionFlowTracer.TRACE_PREFIX
                        + "{\"type\":\"VARIABLE\",\"lineNumber\":3,\"name\":\"arr\",\"value\":\"[3, 1, 2]\","
                        + "\"dataType\":\"int[]\",\"elements\":[\"3\",\"1\",\"2\"]}",
                JavaRuntimeExecutionFlowTracer.TRACE_PREFIX
                        + "{\"type\":\"LOOP\",\"lineNumber\":4}",
                JavaRuntimeExecutionFlowTracer.TRACE_PREFIX
                        + "{\"type\":\"VARIABLE\",\"lineNumber\":4,\"name\":\"i\",\"value\":\"0\",\"dataType\":\"loop\"}",
                JavaRuntimeExecutionFlowTracer.TRACE_PREFIX
                        + "{\"type\":\"ARRAY_UPDATE\",\"lineNumber\":5,\"name\":\"arr\",\"value\":\"[4, 1, 2]\","
                        + "\"index\":0,\"elements\":[\"4\",\"1\",\"2\"]}"
        );
        return new CodeExecutionResult(CodingSubmissionStatus.ACCEPTED, "token", stdout, null, null, null, "0.01", 1024);
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

    private GrammarTemplateChapterMission mission(
        GrammarTemplate template,
        GrammarTemplateChapter chapter,
        com.fasterxml.jackson.databind.JsonNode validationJson
    ) {
        return GrammarTemplateChapterMission.builder()
                .id(100L)
                .template(template)
                .chapter(chapter)
                .title("Mission")
                .description("description")
                .missionType(GrammarTemplateMissionType.MISSION)
                .orderIndex(1)
                .guideContent("guide")
                .validationJson(validationJson)
                .build();
    }
}
