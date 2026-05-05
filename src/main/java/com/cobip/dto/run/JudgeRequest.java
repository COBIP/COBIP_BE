package com.cobip.dto.run;

import lombok.Getter;

@Getter
public class JudgeRequest {

    // 실행할 언어
    private String language;

    // 채점할 코드
    private String code;
}