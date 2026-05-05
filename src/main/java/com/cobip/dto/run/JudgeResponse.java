package com.cobip.dto.run;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JudgeResponse {

    // 정답 여부
    private boolean success;

    // 실행 결과
    private String output;

    // 기대 결과
    private String expected;
}