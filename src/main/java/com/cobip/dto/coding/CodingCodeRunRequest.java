package com.cobip.dto.coding;

import com.cobip.domain.coding.CodingLanguage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CodingCodeRunRequest {

    @NotNull
    private CodingLanguage language;

    @NotBlank
    @Size(max = 20000)
    private String sourceCode;

    @Size(max = 10000)
    private String input;
}
