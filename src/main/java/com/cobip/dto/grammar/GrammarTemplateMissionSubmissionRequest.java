package com.cobip.dto.grammar;

import java.util.List;

import com.cobip.domain.coding.CodingLanguage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GrammarTemplateMissionSubmissionRequest {

    @NotNull
    private CodingLanguage language;

    @NotEmpty
    @Size(max = 200)
    private List<@Valid GrammarTemplateMissionSubmissionFileRequest> submittedCode;
}
