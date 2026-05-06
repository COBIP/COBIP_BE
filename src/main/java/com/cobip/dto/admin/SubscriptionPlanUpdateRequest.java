package com.cobip.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionPlanUpdateRequest {

    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "code must contain only uppercase letters, numbers, underscores, or hyphens.")
    @Size(max = 80, message = "code must be 80 characters or fewer.")
    private String code;

    @Size(max = 80, message = "name must be 80 characters or fewer.")
    private String name;

    private String description;

    @PositiveOrZero(message = "priceAmount must be zero or positive.")
    private Integer priceAmount;

    @Size(max = 10, message = "currency must be 10 characters or fewer.")
    private String currency;

    @Positive(message = "durationDays must be positive.")
    private Integer durationDays;

    private String benefitDescription;

    private Boolean visible;

    @Min(value = 0, message = "displayOrder must be zero or positive.")
    private Integer displayOrder;
}
