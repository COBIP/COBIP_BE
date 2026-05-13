package com.cobip.dto.run;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class JudgeRequest {

    // 실행할 언어
    @NotBlank
    private String language;

    // 채점할 코드
    @JsonAlias("sourceCode")
    @NotBlank
    @Size(max = 20000)
    private String code;
}
