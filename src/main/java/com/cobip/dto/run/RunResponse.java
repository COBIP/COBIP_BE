package com.cobip.dto.run;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RunResponse {

    // 코드 실행 결과
    private String output;
}