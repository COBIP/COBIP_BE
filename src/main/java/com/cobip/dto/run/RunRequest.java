package com.cobip.dto.run;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RunRequest {

    // 실행할 언어 (현재 python)
    @NotBlank
    private String language;

    // 실행할 코드
    @JsonAlias("sourceCode")
    @NotBlank
    @Size(max = 20000)
    private String code;
}
