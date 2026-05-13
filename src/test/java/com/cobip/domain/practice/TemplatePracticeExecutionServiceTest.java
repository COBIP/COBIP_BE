package com.cobip.domain.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.practice.TemplatePracticeCodeRunRequest;
import com.cobip.dto.practice.TemplatePracticeProjectExecutionRequest;
import com.cobip.dto.practice.TemplatePracticeProjectFileRequest;
import com.cobip.dto.practice.TemplatePracticeSubmissionRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TemplatePracticeExecutionServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplatePracticeMissionRepository missionRepository;

    @Mock
    private TemplatePracticeProgressRepository progressRepository;

    @Mock
    private TemplatePracticeMissionProgressRepository missionProgressRepository;

    @Mock
    private TemplatePracticeSubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private CodeExecutionClient codeExecutionClient;

    @Mock
    private ProjectExecutionClient projectExecutionClient;

    private TemplatePracticeExecutionService templatePracticeExecutionService;

    @BeforeEach
    void setUp() {
        templatePracticeExecutionService = new TemplatePracticeExecutionService(
                templateRepository,
                missionRepository,
                progressRepository,
                missionProgressRepository,
                submissionRepository,
                userRepository,
                subscriptionService,
                codeExecutionClient,
                projectExecutionClient,
                objectMapper
        );
    }

    @Test
    void runMissionExecutesCodeWithCustomInputAndMissionLimits() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, validationJson());
        TemplatePracticeCodeRunRequest request = runRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));
        when(codeExecutionClient.execute(
                eq(CodingLanguage.JAVA),
                eq("class Main {}"),
                eq("hello"),
                eq(null),
                eq(3000),
                eq(256)
        )).thenReturn(result(CodingSubmissionStatus.ACCEPTED));

        var response = templatePracticeExecutionService.runMission(user, 10L, 20L, request);

        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
        assertThat(response.getStdout()).isEqualTo("ok");
    }

    @Test
    void submitMissionSavesSubmissionAndCompletesMissionProgress() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, validationJson());
        TemplatePracticeProgress progress = TemplatePracticeProgress.start(user, template, mission);
        TemplatePracticeMissionProgress missionProgress = TemplatePracticeMissionProgress.start(user, mission);
        TemplatePracticeSubmissionRequest request = submissionRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));
        when(codeExecutionClient.execute(eq(CodingLanguage.JAVA), eq("class Main {}"), any(), any(), eq(3000), eq(256)))
                .thenReturn(result(CodingSubmissionStatus.ACCEPTED));
        when(submissionRepository.save(any(TemplatePracticeSubmission.class))).thenAnswer(invocation -> {
            TemplatePracticeSubmission submission = invocation.getArgument(0);
            ReflectionTestUtils.setField(submission, "id", 100L);
            return submission;
        });
        when(progressRepository.findByUserIdAndTemplateId(1L, 10L)).thenReturn(Optional.of(progress));
        when(missionProgressRepository.findByUserIdAndMissionId(1L, 20L)).thenReturn(Optional.of(missionProgress));
        when(missionRepository.countByTemplateId(10L)).thenReturn(2L);
        when(missionProgressRepository.countCompletedByUserAndTemplate(
                1L,
                10L,
                TemplatePracticeMissionProgressStatus.COMPLETED
        )).thenReturn(1L);
        when(missionRepository.findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(10L, 1))
                .thenReturn(Optional.empty());

        var response = templatePracticeExecutionService.submitMission(user, 10L, 20L, request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
        assertThat(response.getPassedCount()).isEqualTo(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(missionProgress.getStatus()).isEqualTo(TemplatePracticeMissionProgressStatus.COMPLETED);
        assertThat(progress.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void submitMissionRejectsMissionWithoutValidationJson() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> templatePracticeExecutionService.submitMission(user, 10L, 20L, submissionRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void runProjectMissionExecutesProjectFilesWithRunCommand() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, projectValidationJson());
        TemplatePracticeProjectExecutionRequest request = projectRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));
        when(projectExecutionClient.execute(any(ProjectExecutionRequest.class)))
                .thenReturn(projectResult(TemplatePracticeSubmissionStatus.ACCEPTED));

        var response = templatePracticeExecutionService.runProjectMission(user, 10L, 20L, request);

        ArgumentCaptor<ProjectExecutionRequest> captor = ArgumentCaptor.forClass(ProjectExecutionRequest.class);
        verify(projectExecutionClient).execute(captor.capture());
        assertThat(captor.getValue().command()).isEqualTo("gradle bootRun --no-daemon");
        assertThat(captor.getValue().timeLimitMillis()).isEqualTo(60000);
        assertThat(captor.getValue().memoryLimitMb()).isEqualTo(512);
        assertThat(captor.getValue().files()).hasSize(2);
        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
    }

    @Test
    void submitProjectMissionSavesSubmissionAndCompletesProgress() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, projectValidationJson());
        TemplatePracticeProgress progress = TemplatePracticeProgress.start(user, template, mission);
        TemplatePracticeMissionProgress missionProgress = TemplatePracticeMissionProgress.start(user, mission);
        TemplatePracticeProjectExecutionRequest request = projectRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));
        when(projectExecutionClient.execute(any(ProjectExecutionRequest.class)))
                .thenReturn(projectResult(TemplatePracticeSubmissionStatus.ACCEPTED));
        when(submissionRepository.save(any(TemplatePracticeSubmission.class))).thenAnswer(invocation -> {
            TemplatePracticeSubmission submission = invocation.getArgument(0);
            ReflectionTestUtils.setField(submission, "id", 101L);
            return submission;
        });
        when(progressRepository.findByUserIdAndTemplateId(1L, 10L)).thenReturn(Optional.of(progress));
        when(missionProgressRepository.findByUserIdAndMissionId(1L, 20L)).thenReturn(Optional.of(missionProgress));
        when(missionRepository.countByTemplateId(10L)).thenReturn(1L);
        when(missionProgressRepository.countCompletedByUserAndTemplate(
                1L,
                10L,
                TemplatePracticeMissionProgressStatus.COMPLETED
        )).thenReturn(1L);
        when(missionRepository.findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(10L, 1))
                .thenReturn(Optional.empty());

        var response = templatePracticeExecutionService.submitProjectMission(user, 10L, 20L, request);

        ArgumentCaptor<ProjectExecutionRequest> captor = ArgumentCaptor.forClass(ProjectExecutionRequest.class);
        verify(projectExecutionClient).execute(captor.capture());
        assertThat(captor.getValue().command()).isEqualTo("gradle test --no-daemon");
        assertThat(captor.getValue().timeLimitMillis()).isEqualTo(60000);
        assertThat(captor.getValue().memoryLimitMb()).isEqualTo(512);
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.ACCEPTED);
        assertThat(response.getPassedCount()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(missionProgress.getStatus()).isEqualTo(TemplatePracticeMissionProgressStatus.COMPLETED);
        assertThat(progress.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void submitProjectMissionSavesInternalErrorWithoutCompletingProgress() {
        User user = user();
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, projectValidationJson());
        TemplatePracticeProgress progress = TemplatePracticeProgress.start(user, template, mission);
        TemplatePracticeMissionProgress missionProgress = TemplatePracticeMissionProgress.start(user, mission);
        TemplatePracticeProjectExecutionRequest request = projectRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 10L)).thenReturn(Optional.of(mission));
        when(projectExecutionClient.execute(any(ProjectExecutionRequest.class)))
                .thenReturn(projectResult(TemplatePracticeSubmissionStatus.INTERNAL_ERROR));
        when(submissionRepository.save(any(TemplatePracticeSubmission.class))).thenAnswer(invocation -> {
            TemplatePracticeSubmission submission = invocation.getArgument(0);
            ReflectionTestUtils.setField(submission, "id", 102L);
            return submission;
        });
        when(progressRepository.findByUserIdAndTemplateId(1L, 10L)).thenReturn(Optional.of(progress));
        when(missionProgressRepository.findByUserIdAndMissionId(1L, 20L)).thenReturn(Optional.of(missionProgress));

        var response = templatePracticeExecutionService.submitProjectMission(user, 10L, 20L, request);

        assertThat(response.getId()).isEqualTo(102L);
        assertThat(response.getStatus()).isEqualTo(TemplatePracticeSubmissionStatus.INTERNAL_ERROR);
        assertThat(response.getPassedCount()).isZero();
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(missionProgress.getStatus()).isEqualTo(TemplatePracticeMissionProgressStatus.IN_PROGRESS);
    }

    private TemplatePracticeCodeRunRequest runRequest() {
        TemplatePracticeCodeRunRequest request = new TemplatePracticeCodeRunRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.JAVA);
        ReflectionTestUtils.setField(request, "sourceCode", "class Main {}");
        ReflectionTestUtils.setField(request, "input", "hello");
        return request;
    }

    private TemplatePracticeProjectExecutionRequest projectRequest() {
        TemplatePracticeProjectExecutionRequest request = new TemplatePracticeProjectExecutionRequest();
        ReflectionTestUtils.setField(request, "files", List.of(
                projectFile("build.gradle", "plugins { id 'java' }"),
                projectFile("src/main/java/com/example/AuthController.java", "class AuthController {}")
        ));
        return request;
    }

    private TemplatePracticeProjectFileRequest projectFile(String filePath, String content) {
        TemplatePracticeProjectFileRequest request = new TemplatePracticeProjectFileRequest();
        ReflectionTestUtils.setField(request, "filePath", filePath);
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    private TemplatePracticeSubmissionRequest submissionRequest() {
        TemplatePracticeSubmissionRequest request = new TemplatePracticeSubmissionRequest();
        ReflectionTestUtils.setField(request, "language", CodingLanguage.JAVA);
        ReflectionTestUtils.setField(request, "sourceCode", "class Main {}");
        return request;
    }

    private CodeExecutionResult result(CodingSubmissionStatus status) {
        return new CodeExecutionResult(status, "token", "ok", null, null, null, "0.01", 1024);
    }

    private ProjectExecutionResult projectResult(TemplatePracticeSubmissionStatus status) {
        return new ProjectExecutionResult(status, 0, "BUILD SUCCESSFUL", "", null, 1200L);
    }

    private JsonNode validationJson() {
        var root = objectMapper.createObjectNode();
        root.put("timeLimitMillis", 3000);
        root.put("memoryLimitMb", 256);
        var testCases = root.putArray("testCases");
        testCases.addObject()
                .put("input", "hello")
                .put("expectedOutput", "ok");
        testCases.addObject()
                .put("input", "world")
                .put("expectedOutput", "ok");
        return root;
    }

    private JsonNode projectValidationJson() {
        var root = objectMapper.createObjectNode();
        root.put("language", "JAVA");
        root.put("dockerImage", "gradle:8.14-jdk21");
        root.put("runCommand", "gradle bootRun --no-daemon");
        root.put("testCommand", "gradle test --no-daemon");
        root.put("timeLimitMillis", 60000);
        root.put("memoryLimitMb", 512);
        return root;
    }

    private TemplatePracticeMission mission(Template template, JsonNode validationJson) {
        return TemplatePracticeMission.builder()
                .id(20L)
                .template(template)
                .title("JWT login")
                .description("Implement login API")
                .missionType(TemplatePracticeMissionType.IMPLEMENTATION)
                .orderIndex(1)
                .guideContent("Use Spring Security")
                .validationJson(validationJson)
                .build();
    }

    private Template template(User owner) {
        return Template.builder()
                .id(10L)
                .owner(owner)
                .title("JWT OAuth2 Login")
                .description("description")
                .category("backend")
                .difficulty(TemplateDifficulty.INTERMEDIATE)
                .techStacks(List.of("Spring", "JWT", "OAuth2"))
                .visibility(TemplateVisibility.PUBLIC)
                .accessLevel(TemplateAccessLevel.FREE)
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
