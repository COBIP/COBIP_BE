package com.cobip.domain.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.coding.CodingLanguage;
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
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeMissionProgressUpdateRequest;
import com.cobip.dto.practice.TemplatePracticeProgressResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TemplatePracticeServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplatePracticeFileRepository fileRepository;

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

    private TemplatePracticeService templatePracticeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        templatePracticeService = new TemplatePracticeService(
                templateRepository,
                fileRepository,
                missionRepository,
                progressRepository,
                missionProgressRepository,
                submissionRepository,
                userRepository,
                subscriptionService,
                objectMapper
        );
    }

    @Test
    void getPracticeReturnsFilesMissionsAndProgress() {
        User owner = user(1L);
        Template template = template(owner);
        TemplatePracticeFile file = file(template);
        TemplatePracticeMission mission = mission(template, 1L, 1);
        TemplatePracticeProgress progress = TemplatePracticeProgress.start(owner, template, mission);
        TemplatePracticeMissionProgress missionProgress = TemplatePracticeMissionProgress.start(owner, mission);
        missionProgress.changeStatus(TemplatePracticeMissionProgressStatus.COMPLETED);
        TemplatePracticeSubmission submission = submission(
                owner,
                template,
                mission,
                """
                        [{"filePath":"src/main/java/com/example/AuthController.java","content":"class AuthController { void login() {} }"}]
                        """
        );
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(fileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(1L)).thenReturn(List.of(file));
        when(missionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(1L)).thenReturn(List.of(mission));
        when(progressRepository.findByUserIdAndTemplateId(1L, 1L)).thenReturn(Optional.of(progress));
        when(missionProgressRepository.findByUserIdAndTemplateId(1L, 1L)).thenReturn(List.of(missionProgress));
        when(submissionRepository.findByUserIdAndTemplateIdOrderByCreatedAtDescIdDesc(1L, 1L))
                .thenReturn(List.of(submission));

        TemplatePracticeDetailResponse response = templatePracticeService.getPractice(owner, 1L);

        assertThat(response.getTemplateId()).isEqualTo(1L);
        assertThat(response.getFiles()).hasSize(1);
        assertThat(response.getMissions()).hasSize(1);
        assertThat(response.getProgress().getStatus()).isEqualTo(TemplatePracticeProgressStatus.IN_PROGRESS);
        assertThat(response.getFiles().get(0).getUserContent())
                .isEqualTo("class AuthController { void login() {} }");
        assertThat(response.getMissions().get(0).getProgressStatus())
                .isEqualTo(TemplatePracticeMissionProgressStatus.COMPLETED);
    }

    @Test
    void startPracticeCreatesProgressWithFirstMission() {
        User user = user(1L);
        Template template = template(user);
        TemplatePracticeMission mission = mission(template, 1L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(missionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(1L)).thenReturn(List.of(mission));
        when(progressRepository.findByUserIdAndTemplateId(1L, 1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(TemplatePracticeProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TemplatePracticeProgressResponse response = templatePracticeService.startPractice(user, 1L);

        ArgumentCaptor<TemplatePracticeProgress> progressCaptor = ArgumentCaptor.forClass(TemplatePracticeProgress.class);
        verify(progressRepository).save(progressCaptor.capture());
        assertThat(progressCaptor.getValue().getCurrentMission()).isEqualTo(mission);
        assertThat(response.getStatus()).isEqualTo(TemplatePracticeProgressStatus.IN_PROGRESS);
    }

    @Test
    void updateMissionProgressUpdatesOverallProgress() {
        User user = user(1L);
        Template template = template(user);
        TemplatePracticeMission firstMission = mission(template, 1L, 1);
        TemplatePracticeMission nextMission = mission(template, 2L, 2);
        TemplatePracticeProgress progress = TemplatePracticeProgress.start(user, template, firstMission);
        TemplatePracticeMissionProgress missionProgress = TemplatePracticeMissionProgress.start(user, firstMission);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(1L, 1L)).thenReturn(Optional.of(firstMission));
        when(progressRepository.findByUserIdAndTemplateId(1L, 1L)).thenReturn(Optional.of(progress));
        when(missionProgressRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(Optional.of(missionProgress));
        when(missionRepository.countByTemplateId(1L)).thenReturn(2L);
        when(missionProgressRepository.countCompletedByUserAndTemplate(
                1L,
                1L,
                TemplatePracticeMissionProgressStatus.COMPLETED
        )).thenReturn(1L);
        when(missionRepository.findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(1L, 1))
                .thenReturn(Optional.of(nextMission));

        TemplatePracticeProgressResponse response = templatePracticeService.updateMissionProgress(
                user,
                1L,
                1L,
                request(TemplatePracticeMissionProgressStatus.COMPLETED)
        );

        assertThat(response.getProgressPercent()).isEqualTo(50);
        assertThat(response.getCompletedMissionCount()).isEqualTo(1);
        assertThat(response.getCurrentMissionId()).isEqualTo(2L);
    }

    private TemplatePracticeMissionProgressUpdateRequest request(TemplatePracticeMissionProgressStatus status) {
        TemplatePracticeMissionProgressUpdateRequest request = new TemplatePracticeMissionProgressUpdateRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    private TemplatePracticeFile file(Template template) {
        return TemplatePracticeFile.builder()
                .id(1L)
                .template(template)
                .filePath("src/main/java/com/example/AuthController.java")
                .language("JAVA")
                .content("class AuthController {}")
                .readOnly(false)
                .orderIndex(1)
                .build();
    }

    private TemplatePracticeSubmission submission(
        User user,
        Template template,
        TemplatePracticeMission mission,
        String sourceCode
    ) {
        return TemplatePracticeSubmission.builder()
                .id(1L)
                .user(user)
                .template(template)
                .mission(mission)
                .language(CodingLanguage.JAVA)
                .sourceCode(sourceCode)
                .status(TemplatePracticeSubmissionStatus.ACCEPTED)
                .passedCount(1)
                .totalCount(1)
                .build();
    }

    private TemplatePracticeMission mission(Template template, Long id, int orderIndex) {
        return TemplatePracticeMission.builder()
                .id(id)
                .template(template)
                .title("JWT login")
                .description("Implement login API")
                .missionType(TemplatePracticeMissionType.IMPLEMENTATION)
                .orderIndex(orderIndex)
                .guideContent("Use Spring Security")
                .build();
    }

    private Template template(User owner) {
        return Template.builder()
                .id(1L)
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

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("encoded-password")
                .nickname("user" + id)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
