package com.cobip.domain.moderation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateRepository;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.admin.AdminContentModerationRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminContentModerationServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private GrammarTemplateRepository grammarTemplateRepository;

    @Mock
    private ActivityHistoryService activityHistoryService;

    private AdminContentModerationService adminContentModerationService;

    @BeforeEach
    void setUp() {
        adminContentModerationService = new AdminContentModerationService(
                templateRepository,
                grammarTemplateRepository,
                activityHistoryService
        );
    }

    @Test
    void moderateBlindsTemplate() {
        User adminUser = user(1L, UserRole.ADMIN);
        Template template = template(10L);
        AdminContentModerationRequest request = request(
                ContentModerationTargetType.TEMPLATE,
                10L,
                ContentModerationAction.BLIND
        );
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(template));

        adminContentModerationService.moderate(request, adminUser);

        assertThat(template.getVisibility()).isEqualTo(TemplateVisibility.PRIVATE);
        assertThat(template.getAccessLevel()).isEqualTo(TemplateAccessLevel.FREE);
        verify(activityHistoryService).record(
                eq(adminUser),
                eq(ActivityType.CONTENT_MODERATED),
                eq("Admin moderated content: BLIND."),
                eq("TEMPLATE"),
                eq(10L)
        );
    }

    @Test
    void moderateDeletesGrammarTemplate() {
        GrammarTemplate template = grammarTemplate(20L);
        AdminContentModerationRequest request = request(
                ContentModerationTargetType.GRAMMAR_TEMPLATE,
                20L,
                ContentModerationAction.DELETE
        );
        when(grammarTemplateRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(template));

        adminContentModerationService.moderate(request, null);

        assertThat(template.getDeletedAt()).isNotNull();
    }

    @Test
    void moderateRejectsMissingTemplate() {
        AdminContentModerationRequest request = request(
                ContentModerationTargetType.TEMPLATE,
                10L,
                ContentModerationAction.DELETE
        );
        when(templateRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminContentModerationService.moderate(request, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);
    }

    private AdminContentModerationRequest request(
        ContentModerationTargetType targetType,
        Long targetId,
        ContentModerationAction action
    ) {
        AdminContentModerationRequest request = new AdminContentModerationRequest();
        ReflectionTestUtils.setField(request, "targetType", targetType);
        ReflectionTestUtils.setField(request, "targetId", targetId);
        ReflectionTestUtils.setField(request, "action", action);
        ReflectionTestUtils.setField(request, "adminMemo", "Inappropriate content");
        return request;
    }

    private Template template(Long id) {
        return Template.builder()
                .id(id)
                .owner(user(2L, UserRole.USER))
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

    private GrammarTemplate grammarTemplate(Long id) {
        return GrammarTemplate.builder()
                .id(id)
                .slug("java-basic")
                .title("Java Basic")
                .language(GrammarTemplateLanguage.JAVA)
                .category("basic")
                .difficulty(GrammarTemplateDifficulty.BEGINNER)
                .summary("summary")
                .searchableText("java")
                .status(GrammarTemplateStatus.PUBLISHED)
                .build();
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("password")
                .nickname("user" + id)
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
