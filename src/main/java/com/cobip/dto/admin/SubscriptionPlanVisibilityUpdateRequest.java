package com.cobip.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionPlanVisibilityUpdateRequest {

    @NotNull(message = "visible is required.")
    private Boolean visible;

    public boolean isVisible() {
        return Boolean.TRUE.equals(visible);
    }
}
