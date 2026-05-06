package com.cobip.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.certificate.CertificateRepository;
import com.cobip.domain.grammar.GrammarTemplateRepository;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.admin.AdminOperationStatisticsResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private GrammarTemplateRepository grammarTemplateRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ActivityHistoryRepository activityHistoryRepository;

    private AdminStatisticsService adminStatisticsService;

    @BeforeEach
    void setUp() {
        adminStatisticsService = new AdminStatisticsService(
                userRepository,
                subscriptionRepository,
                templateRepository,
                grammarTemplateRepository,
                learningProgressRepository,
                certificateRepository,
                activityHistoryRepository
        );
    }

    @Test
    void getOverviewReturnsAggregatedCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(subscriptionRepository.countActiveSubscriptions(any())).thenReturn(3L);
        when(templateRepository.countByDeletedAtIsNull()).thenReturn(7L);
        when(templateRepository.countByDeletedAtIsNullAndVisibility(TemplateVisibility.PUBLIC)).thenReturn(5L);
        when(templateRepository.countByDeletedAtIsNullAndAccessLevel(TemplateAccessLevel.PREMIUM)).thenReturn(2L);
        when(grammarTemplateRepository.countByDeletedAtIsNull()).thenReturn(4L);
        when(grammarTemplateRepository.countByDeletedAtIsNullAndStatus(GrammarTemplateStatus.PUBLISHED)).thenReturn(3L);
        when(learningProgressRepository.count()).thenReturn(8L);
        when(learningProgressRepository.countByProgressPercentGreaterThanEqual(100)).thenReturn(6L);
        when(learningProgressRepository.sumSolvedCount()).thenReturn(40L);
        when(learningProgressRepository.sumCorrectCount()).thenReturn(35L);
        when(learningProgressRepository.sumStudySeconds()).thenReturn(1200L);
        when(certificateRepository.count()).thenReturn(9L);
        when(activityHistoryRepository.count()).thenReturn(11L);

        AdminOperationStatisticsResponse response = adminStatisticsService.getOverview();

        assertThat(response.userCount()).isEqualTo(10L);
        assertThat(response.activeSubscriptionCount()).isEqualTo(3L);
        assertThat(response.templateCount()).isEqualTo(7L);
        assertThat(response.publicTemplateCount()).isEqualTo(5L);
        assertThat(response.premiumTemplateCount()).isEqualTo(2L);
        assertThat(response.grammarTemplateCount()).isEqualTo(4L);
        assertThat(response.publishedGrammarTemplateCount()).isEqualTo(3L);
        assertThat(response.learningProgressCount()).isEqualTo(8L);
        assertThat(response.completedLearningProgressCount()).isEqualTo(6L);
        assertThat(response.totalSolvedCount()).isEqualTo(40L);
        assertThat(response.totalCorrectCount()).isEqualTo(35L);
        assertThat(response.totalStudySeconds()).isEqualTo(1200L);
        assertThat(response.certificateCount()).isEqualTo(9L);
        assertThat(response.activityHistoryCount()).isEqualTo(11L);
    }
}
