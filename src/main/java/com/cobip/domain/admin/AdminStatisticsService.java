package com.cobip.domain.admin;

import java.time.LocalDate;

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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TemplateRepository templateRepository;
    private final GrammarTemplateRepository grammarTemplateRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final CertificateRepository certificateRepository;
    private final ActivityHistoryRepository activityHistoryRepository;

    @Transactional(readOnly = true)
    public AdminOperationStatisticsResponse getOverview() {
        return new AdminOperationStatisticsResponse(
                userRepository.count(),
                subscriptionRepository.countActiveSubscriptions(LocalDate.now()),
                templateRepository.countByDeletedAtIsNull(),
                templateRepository.countByDeletedAtIsNullAndVisibility(TemplateVisibility.PUBLIC),
                templateRepository.countByDeletedAtIsNullAndAccessLevel(TemplateAccessLevel.PREMIUM),
                grammarTemplateRepository.countByDeletedAtIsNull(),
                grammarTemplateRepository.countByDeletedAtIsNullAndStatus(GrammarTemplateStatus.PUBLISHED),
                learningProgressRepository.count(),
                learningProgressRepository.countByProgressPercentGreaterThanEqual(100),
                learningProgressRepository.sumSolvedCount(),
                learningProgressRepository.sumCorrectCount(),
                learningProgressRepository.sumStudySeconds(),
                certificateRepository.count(),
                activityHistoryRepository.count()
        );
    }
}
