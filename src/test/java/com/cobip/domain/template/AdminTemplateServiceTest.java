package com.cobip.domain.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.practice.TemplatePracticeFile;
import com.cobip.domain.practice.TemplatePracticeFileRepository;
import com.cobip.domain.practice.TemplatePracticeMission;
import com.cobip.domain.practice.TemplatePracticeMissionRepository;
import com.cobip.domain.practice.TemplatePracticeMissionType;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.dto.admin.AdminTemplateDetailResponse;
import com.cobip.dto.admin.AdminTemplateExposureUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminTemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplatePracticeFileRepository practiceFileRepository;

    @Mock
    private TemplatePracticeMissionRepository practiceMissionRepository;

    @Mock
    private TemplateTestCaseRepository templateTestCaseRepository;

    @Mock
    private ActivityHistoryService activityHistoryService;

    private AdminTemplateService adminTemplateService;

    @BeforeEach
    void setUp() {
        adminTemplateService = new AdminTemplateService(
                templateRepository,
                practiceFileRepository,
                practiceMissionRepository,
                templateTestCaseRepository,
                activityHistoryService
        );
    }

    @Test
    void getTemplateIncludesEditorInitialValues() {
        Template template = template(10L, user(2L, UserRole.USER));
        TemplatePracticeFile file = TemplatePracticeFile.builder()
                .id(100L)
                .template(template)
                .filePath("src/main.ts")
                .language("typescript")
                .content("console.log('hello')")
                .readOnly(false)
                .orderIndex(0)
                .build();
        TemplatePracticeMission mission = TemplatePracticeMission.builder()
                .id(200L)
                .template(template)
                .title("JWT 구현")
                .description("JWT 로그인을 구현합니다.")
                .missionType(TemplatePracticeMissionType.IMPLEMENTATION)
                .orderIndex(0)
                .build();
        TemplateTestCase testCase = TemplateTestCase.builder()
                .id(300L)
                .template(template)
                .input("1 2")
                .expectedOutput("3")
                .description("기본 케이스")
                .orderIndex(0)
                .build();
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        when(practiceFileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(10L)).thenReturn(List.of(file));
        when(practiceMissionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(10L)).thenReturn(List.of(mission));
        when(templateTestCaseRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(10L)).thenReturn(List.of(testCase));

        AdminTemplateDetailResponse response = adminTemplateService.getTemplate(10L);

        assertThat(response.getSummary()).isEqualTo("Summary");
        assertThat(response.getRuntime()).isEqualTo("node20");
        assertThat(response.getTags()).containsExactly("auth");
        assertThat(response.getPreviewImage()).isEqualTo("https://cdn.example.com/preview.png");
        assertThat(response.isPublished()).isTrue();
        assertThat(response.getInterviewQuestions()).hasSize(1);
        assertThat(response.getInterviewQuestions().get(0).getAnswerHint()).isEqualTo("상태 비저장 인증");
        assertThat(response.getPracticeFiles()).hasSize(1);
        assertThat(response.getPracticeFiles().get(0).getPath()).isEqualTo("src/main.ts");
        assertThat(response.getPracticeFiles().get(0).getName()).isEqualTo("main.ts");
        assertThat(response.getPracticeFiles().get(0).getContent()).isEqualTo("console.log('hello')");
        assertThat(response.getMissions()).hasSize(1);
        assertThat(response.getTestCases()).hasSize(1);
        assertThat(response.getTestCases().get(0).getExpectedOutput()).isEqualTo("3");
    }

    @Test
    void updateExposureChangesVisibilityAndAccessLevel() {
        User adminUser = user(1L, UserRole.ADMIN);
        Template template = template(10L, user(2L, UserRole.USER));
        AdminTemplateExposureUpdateRequest request = exposureRequest(
                TemplateVisibility.PRIVATE,
                TemplateAccessLevel.PREMIUM
        );
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));
        mockEmptyDetailChildren(10L);

        adminTemplateService.updateExposure(10L, request, adminUser);

        assertThat(template.getVisibility()).isEqualTo(TemplateVisibility.PRIVATE);
        assertThat(template.getAccessLevel()).isEqualTo(TemplateAccessLevel.PREMIUM);
        verify(activityHistoryService).record(
                eq(adminUser),
                eq(ActivityType.TEMPLATE_UPDATED),
                eq("Admin updated template exposure."),
                eq("TEMPLATE"),
                eq(10L)
        );
    }

    @Test
    void updateExposureRejectsMissingTemplate() {
        AdminTemplateExposureUpdateRequest request = exposureRequest(
                TemplateVisibility.PRIVATE,
                TemplateAccessLevel.PREMIUM
        );
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminTemplateService.updateExposure(10L, request, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);
    }

    private AdminTemplateExposureUpdateRequest exposureRequest(
        TemplateVisibility visibility,
        TemplateAccessLevel accessLevel
    ) {
        AdminTemplateExposureUpdateRequest request = new AdminTemplateExposureUpdateRequest();
        ReflectionTestUtils.setField(request, "visibility", visibility);
        ReflectionTestUtils.setField(request, "accessLevel", accessLevel);
        return request;
    }

    private Template template(Long id, User owner) {
        return Template.builder()
                .id(id)
                .owner(owner)
                .title("Template")
                .description("Description")
                .category("backend")
                .difficulty(TemplateDifficulty.BEGINNER)
                .techStacks(List.of("Spring"))
                .visibility(TemplateVisibility.PUBLIC)
                .accessLevel(TemplateAccessLevel.FREE)
                .summary("Summary")
                .tags(List.of("auth"))
                .runtime("node20")
                .license("MIT")
                .source("internal")
                .thumbnailUrl("https://cdn.example.com/preview.png")
                .interviewQuestions(List.of(TemplateInterviewQuestion.of("JWT를 사용하는 이유는?", "상태 비저장 인증")))
                .viewCount(0)
                .favoriteCount(0)
                .build();
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("password")
                .nickname("user" + id)
                .role(role)
                .emailVerified(true)
                .build();
    }

    private void mockEmptyDetailChildren(Long templateId) {
        when(practiceFileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(templateId)).thenReturn(List.of());
        when(practiceMissionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(templateId)).thenReturn(List.of());
        when(templateTestCaseRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(templateId)).thenReturn(List.of());
    }
}
