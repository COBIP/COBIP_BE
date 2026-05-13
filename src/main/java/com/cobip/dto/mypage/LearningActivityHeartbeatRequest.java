package com.cobip.dto.mypage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class LearningActivityHeartbeatRequest {

    @Positive(message = "templateId must be positive.")
    private Long templateId;

    @NotNull(message = "activeSeconds is required.")
    @Positive(message = "activeSeconds must be positive.")
    @Max(value = 60, message = "activeSeconds must be 60 or less.")
    private Integer activeSeconds;
}
