package com.cobip.dto.admin;

import com.cobip.domain.coding.CodingLanguage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCodingProblemStarterCodeRequest {

    @NotNull(message = "Language is required.")
    private CodingLanguage language;

    @NotBlank(message = "Code is required.")
    private String code;
}
