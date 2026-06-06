package com.cobip.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateNextRecommendationRequest {

    @NotBlank(message = "Feature name is required.")
    @Size(max = 120, message = "Feature name must be 120 characters or less.")
    private String featureName;

    @NotBlank(message = "Reason is required.")
    private String reason;

    @NotBlank(message = "Expected learning is required.")
    private String expectedLearning;

    @NotNull(message = "Priority is required.")
    @Min(value = 1, message = "Priority must be 1 or greater.")
    private Integer priority;
}
