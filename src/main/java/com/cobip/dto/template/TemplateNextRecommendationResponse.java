package com.cobip.dto.template;

import com.cobip.domain.template.TemplateNextRecommendation;

import lombok.Getter;

@Getter
public class TemplateNextRecommendationResponse {

    private final String featureName;
    private final String reason;
    private final String expectedLearning;
    private final int priority;

    private TemplateNextRecommendationResponse(TemplateNextRecommendation nextRecommendation) {
        this.featureName = nextRecommendation.getFeatureName();
        this.reason = nextRecommendation.getReason();
        this.expectedLearning = nextRecommendation.getExpectedLearning();
        this.priority = nextRecommendation.getPriority();
    }

    public static TemplateNextRecommendationResponse from(TemplateNextRecommendation nextRecommendation) {
        return new TemplateNextRecommendationResponse(nextRecommendation);
    }
}
