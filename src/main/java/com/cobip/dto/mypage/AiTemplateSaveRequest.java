package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiTemplateSaveRequest {

    @NotBlank
    @Size(max = 120)
    private String aiTemplateId;

    @NotBlank
    @Size(max = 120)
    private String templateTitle;

    @NotNull
    private JsonNode templateSnapshot;

    private JsonNode sections;

    private JsonNode lastLearningPosition;

    @PositiveOrZero
    @Max(100)
    private Integer progressPercent;

    @Size(max = 255)
    private String lastStep;

    @PositiveOrZero
    private Long studySeconds;

    private Boolean completed;

    private LocalDateTime lastAccessedAt;

    public int getProgressPercentOrDefault() {
        return progressPercent == null ? 0 : progressPercent;
    }

    public long getStudySecondsOrDefault() {
        return studySeconds == null ? 0L : studySeconds;
    }
}
