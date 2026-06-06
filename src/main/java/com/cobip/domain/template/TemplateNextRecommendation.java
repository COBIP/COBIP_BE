package com.cobip.domain.template;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateNextRecommendation {

    @Column(name = "feature_name", nullable = false, length = 120)
    private String featureName;

    @Lob
    private String reason;

    @Lob
    @Column(name = "expected_learning")
    private String expectedLearning;

    @Column(nullable = false)
    private int priority;

    public static TemplateNextRecommendation of(
        String featureName,
        String reason,
        String expectedLearning,
        int priority
    ) {
        return new TemplateNextRecommendation(featureName, reason, expectedLearning, priority);
    }
}
