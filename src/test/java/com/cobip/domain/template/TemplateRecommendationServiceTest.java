package com.cobip.domain.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.infra.aws.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TemplateRecommendationServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFavoriteRepository templateFavoriteRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private ActivityHistoryService activityHistoryService;

    @Mock
    private S3Service s3Service;

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService(
                templateRepository,
                templateFavoriteRepository,
                learningProgressRepository,
                userRepository,
                subscriptionService,
                activityHistoryService,
                s3Service
        );
    }

    @Test
    void getRecommendedTemplatesReturnsPopularTemplatesWhenUserIsAnonymous() {
        PageRequest pageable = PageRequest.of(0, 10);
        Template template = template(1L, "backend");
        when(templateRepository.findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
                TemplateVisibility.PUBLIC,
                pageable
        )).thenReturn(new PageImpl<>(List.of(template), pageable, 1));

        PageResponse<TemplateSummaryResponse> response = templateService.getRecommendedTemplates(null, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getId()).isEqualTo(1L);
        verify(templateRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getRecommendedTemplatesUsesLearningAndFavoriteCategories() {
        User user = user(1L);
        PageRequest pageable = PageRequest.of(0, 10);
        Template learningTemplate = template(1L, "java");
        Template favoriteTemplate = template(2L, "backend");
        Template recommendedTemplate = template(3L, "java");
        when(learningProgressRepository.findTop5ByUserIdOrderByLastAccessedAtDesc(1L))
                .thenReturn(List.of(learningProgress(user, learningTemplate)));
        when(templateFavoriteRepository.findByUserId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(templateFavorite(user, favoriteTemplate))));
        when(templateRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(recommendedTemplate), pageable, 1));

        PageResponse<TemplateSummaryResponse> response = templateService.getRecommendedTemplates(user, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getId()).isEqualTo(3L);
        verify(templateRepository).findAll(any(Specification.class), eq(pageable));
        verify(templateRepository, never()).findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
                any(),
                eq(pageable)
        );
    }

    private TemplateFavorite templateFavorite(User user, Template template) {
        return TemplateFavorite.builder()
                .id(1L)
                .user(user)
                .template(template)
                .build();
    }

    private LearningProgress learningProgress(User user, Template template) {
        return LearningProgress.builder()
                .id(1L)
                .user(user)
                .template(template)
                .progressPercent(50)
                .solvedCount(1)
                .correctCount(1)
                .studySeconds(100)
                .build();
    }

    private Template template(Long id, String category) {
        return Template.builder()
                .id(id)
                .owner(user(99L))
                .title("Template " + id)
                .description("description")
                .category(category)
                .difficulty(TemplateDifficulty.BEGINNER)
                .techStacks(List.of("Spring"))
                .visibility(TemplateVisibility.PUBLIC)
                .accessLevel(TemplateAccessLevel.FREE)
                .viewCount(10)
                .favoriteCount(5)
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
