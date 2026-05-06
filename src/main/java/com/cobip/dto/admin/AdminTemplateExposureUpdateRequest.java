package com.cobip.dto.admin;

import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateVisibility;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateExposureUpdateRequest {

    @NotNull(message = "visibility is required.")
    private TemplateVisibility visibility;

    @NotNull(message = "accessLevel is required.")
    private TemplateAccessLevel accessLevel;
}
