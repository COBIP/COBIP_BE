package com.cobip.dto.admin;

public record AdminOperationStatisticsResponse(
    long userCount,
    long activeSubscriptionCount,
    long templateCount,
    long publicTemplateCount,
    long premiumTemplateCount,
    long grammarTemplateCount,
    long publishedGrammarTemplateCount,
    long learningProgressCount,
    long completedLearningProgressCount,
    long totalSolvedCount,
    long totalCorrectCount,
    long totalStudySeconds,
    long certificateCount,
    long activityHistoryCount
) {
}
