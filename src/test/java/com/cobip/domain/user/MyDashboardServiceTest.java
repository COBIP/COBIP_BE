package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.activity.ActivityHistory;
import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.learning.AiTemplateProgress;
import com.cobip.domain.learning.AiTemplateProgressRepository;
import com.cobip.domain.learning.GrammarLearningProgressRepository;
import com.cobip.domain.learning.GrammarLearningProgressService;
import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.learning.UserLearningDailyStat;
import com.cobip.domain.learning.UserLearningDailyStatRepository;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateFavoriteRepository;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.dto.mypage.MyDashboardResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyDashboardServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFavoriteRepository templateFavoriteRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private AiTemplateProgressRepository aiTemplateProgressRepository;

    @Mock
    private GrammarLearningProgressRepository grammarLearningProgressRepository;

    @Mock
    private GrammarLearningProgressService grammarLearningProgressService;

    @Mock
    private UserLearningDailyStatRepository userLearningDailyStatRepository;

    @Mock
    private ActivityHistoryRepository activityHistoryRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageService(
                userRepository,
                templateRepository,
                templateFavoriteRepository,
                learningProgressRepository,
                aiTemplateProgressRepository,
                grammarLearningProgressRepository,
                grammarLearningProgressService,
                userLearningDailyStatRepository,
                activityHistoryRepository,
                subscriptionRepository,
                passwordEncoder
        );
    }

    @Test
    void getDashboardIncludesContinueLearningAndWeeklyActivities() {
        User user = user();
        Template template = template();
        LearningProgress progress = learningProgress(user, template);
        AiTemplateProgress aiTemplateProgress = aiTemplateProgress(user);
        LocalDate today = LocalDate.now();
        ActivityHistory yesterdayActivity = activity(user, today.minusDays(1).atTime(10, 0));
        ActivityHistory todayActivity = activity(user, today.atTime(11, 0));
        UserLearningDailyStat todayStat = dailyStat(user, today, 1800);
        when(learningProgressRepository.findByUserId(1L)).thenReturn(List.of(progress));
        when(aiTemplateProgressRepository.findByUserIdOrderByLastAccessedAtDesc(1L)).thenReturn(List.of(aiTemplateProgress));
        when(userLearningDailyStatRepository.sumStudySecondsByUserId(1L)).thenReturn(1800L);
        when(learningProgressRepository.findTop5ByUserIdOrderByLastAccessedAtDesc(1L)).thenReturn(List.of(progress));
        when(grammarLearningProgressRepository.findTop5ByUserIdOrderByLastAccessedAtDesc(1L)).thenReturn(List.of());
        when(aiTemplateProgressRepository.findTop5ByUserIdOrderByLastAccessedAtDesc(1L)).thenReturn(List.of(aiTemplateProgress));
        when(activityHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(todayActivity));
        when(activityHistoryRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(yesterdayActivity, todayActivity));
        when(userLearningDailyStatRepository.findByUserIdAndActivityDateBetween(
                eq(1L),
                eq(today.minusDays(6)),
                eq(today)
        )).thenReturn(List.of(todayStat));
        when(templateRepository.findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
                eq(TemplateVisibility.PUBLIC),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(template)));
        when(learningProgressRepository.countByUserIdAndProgressPercentLessThan(1L, 100)).thenReturn(1L);
        when(grammarLearningProgressRepository.countByUserIdAndProgressPercentLessThan(1L, 100)).thenReturn(0L);
        when(aiTemplateProgressRepository.countByUserIdAndProgressPercentLessThan(1L, 100)).thenReturn(1L);
        when(learningProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(1L, 100)).thenReturn(0L);
        when(grammarLearningProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(1L, 100)).thenReturn(0L);
        when(aiTemplateProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(1L, 100)).thenReturn(0L);

        MyDashboardResponse response = myPageService.getDashboard(user);

        assertThat(response.getContinueLearning()).isNotNull();
        assertThat(response.getContinueLearning().getContentType()).isEqualTo("AI_TEMPLATE");
        assertThat(response.getContinueLearning().getAiTemplateId()).isEqualTo("ai-jwt-login");
        assertThat(response.getTotalStudySeconds()).isEqualTo(5700);
        assertThat(response.getWeeklyActivities()).hasSize(7);
        assertThat(response.getWeeklyActivities().get(5).getDate()).isEqualTo(today.minusDays(1));
        assertThat(response.getWeeklyActivities().get(5).getActivityCount()).isEqualTo(1);
        assertThat(response.getWeeklyActivities().get(6).getDate()).isEqualTo(today);
        assertThat(response.getWeeklyActivities().get(6).getActivityCount()).isEqualTo(1);
        assertThat(response.getWeeklyActivities().get(6).getStudySeconds()).isEqualTo(1800);
    }

    private ActivityHistory activity(User user, LocalDateTime createdAt) {
        ActivityHistory activity = ActivityHistory.builder()
                .id(1L)
                .user(user)
                .type(ActivityType.TEMPLATE_CREATED)
                .message("activity")
                .targetType("TEMPLATE")
                .targetId(1L)
                .build();
        ReflectionTestUtils.setField(activity, "createdAt", createdAt);
        return activity;
    }

    private LearningProgress learningProgress(User user, Template template) {
        return LearningProgress.builder()
                .id(1L)
                .user(user)
                .template(template)
                .progressPercent(50)
                .lastStep("01. intro")
                .solvedCount(10)
                .correctCount(8)
                .studySeconds(3600)
                .lastAccessedAt(LocalDateTime.now())
                .build();
    }

    private AiTemplateProgress aiTemplateProgress(User user) {
        return AiTemplateProgress.create(
                user,
                "ai-jwt-login",
                "JWT 로그인 구현",
                objectMapper.createObjectNode().put("title", "JWT 로그인 구현"),
                null,
                null,
                40,
                "section-2",
                300,
                false,
                LocalDateTime.now().plusMinutes(1)
        );
    }

    private UserLearningDailyStat dailyStat(User user, LocalDate activityDate, long studySeconds) {
        return UserLearningDailyStat.builder()
                .id(1L)
                .user(user)
                .activityDate(activityDate)
                .studySeconds(studySeconds)
                .build();
    }

    private Template template() {
        return Template.builder()
                .id(1L)
                .owner(user())
                .title("Template")
                .description("description")
                .category("backend")
                .difficulty(TemplateDifficulty.BEGINNER)
                .techStacks(List.of("Spring"))
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
