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
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
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
    private ActivityHistoryService activityHistoryService;

    private AdminTemplateService adminTemplateService;

    @BeforeEach
    void setUp() {
        adminTemplateService = new AdminTemplateService(templateRepository, activityHistoryService);
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
}
