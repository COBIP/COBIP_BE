package com.cobip.dto.lab;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LabWorkspaceSaveRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 40)
    private String language;

    @NotBlank
    @Size(max = 255)
    private String activeFilePath;

    @NotNull
    private JsonNode files;
}
