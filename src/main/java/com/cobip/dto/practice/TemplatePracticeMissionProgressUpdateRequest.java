package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeMissionProgressStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TemplatePracticeMissionProgressUpdateRequest {

    @NotNull
    private TemplatePracticeMissionProgressStatus status;
}
