package com.cobip.dto.run;

import lombok.Getter;

@Getter
public class RunRequest {

    // 실행할 언어 (현재 python)
    private String language;

    // 실행할 코드
    private String code;
}