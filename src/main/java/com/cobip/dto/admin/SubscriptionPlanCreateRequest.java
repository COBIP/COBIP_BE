package com.cobip.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionPlanCreateRequest {

    @NotBlank(message = "code is required.")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "code must contain only uppercase letters, numbers, underscores, or hyphens.")
    @Size(max = 80, message = "code must be 80 characters or fewer.")
    private String code;

    @NotBlank(message = "name is required.")
    @Size(max = 80, message = "name must be 80 characters or fewer.")
    private String name;

    @NotBlank(message = "description is required.")
    private String description;

    @NotNull(message = "priceAmount is required.")
    @PositiveOrZero(message = "priceAmount must be zero or positive.")
    private Integer priceAmount;

    @NotBlank(message = "currency is required.")
    @Size(max = 10, message = "currency must be 10 characters or fewer.")
    private String currency = "KRW";

    @NotNull(message = "durationDays is required.")
    @Positive(message = "durationDays must be positive.")
    private Integer durationDays;

    @NotBlank(message = "benefitDescription is required.")
    private String benefitDescription;

    @NotNull(message = "visible is required.")
    private Boolean visible;

    @NotNull(message = "displayOrder is required.")
    @Min(value = 0, message = "displayOrder must be zero or positive.")
    private Integer displayOrder;
}
