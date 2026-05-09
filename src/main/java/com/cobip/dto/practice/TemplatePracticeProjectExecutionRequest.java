package com.cobip.dto.practice;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TemplatePracticeProjectExecutionRequest {

    @NotEmpty
    @Size(max = 200)
    private List<@Valid TemplatePracticeProjectFileRequest> files;
}
