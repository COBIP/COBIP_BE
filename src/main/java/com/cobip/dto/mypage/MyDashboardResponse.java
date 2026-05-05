package com.cobip.dto.mypage;

import java.util.List;

import com.cobip.dto.template.TemplateSummaryResponse;

import lombok.Getter;

@Getter
public class MyDashboardResponse {

    private final long registeredTemplateCount;
    private final long inProgressLearningCount;
    private final long completedLearningCount;
    private final long totalStudySeconds;
    private final double averageCorrectRate;
    private final SubscriptionResponse subscription;
    private final List<TemplateSummaryResponse> popularTemplates;
    private final List<LearningProgressResponse> recentLearning;
    private final List<ActivityHistoryResponse> recentActivities;

    public MyDashboardResponse(
        long registeredTemplateCount,
        long inProgressLearningCount,
        long completedLearningCount,
        long totalStudySeconds,
        double averageCorrectRate,
        SubscriptionResponse subscription,
        List<TemplateSummaryResponse> popularTemplates,
        List<LearningProgressResponse> recentLearning,
        List<ActivityHistoryResponse> recentActivities
    ) {
        this.registeredTemplateCount = registeredTemplateCount;
        this.inProgressLearningCount = inProgressLearningCount;
        this.completedLearningCount = completedLearningCount;
        this.totalStudySeconds = totalStudySeconds;
        this.averageCorrectRate = averageCorrectRate;
        this.subscription = subscription;
        this.popularTemplates = popularTemplates;
        this.recentLearning = recentLearning;
        this.recentActivities = recentActivities;
    }
}
