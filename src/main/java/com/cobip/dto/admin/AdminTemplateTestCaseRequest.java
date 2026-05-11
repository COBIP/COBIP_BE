package com.cobip.dto.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminTemplateTestCaseRequest {

    private String input;

    @JsonAlias("expectedOutput")
    @JsonProperty("expected_output")
    private String expectedOutput;

    @Size(max = 1000, message = "Description must be 1000 characters or less.")
    private String description;

    private Integer orderIndex;
}
