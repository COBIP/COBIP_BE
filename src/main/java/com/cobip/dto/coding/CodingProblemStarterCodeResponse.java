package com.cobip.dto.coding;

import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingProblemStarterCode;

import lombok.Getter;

@Getter
public class CodingProblemStarterCodeResponse {

    private final CodingLanguage language;
    private final String code;

    private CodingProblemStarterCodeResponse(CodingProblemStarterCode starterCode) {
        this.language = starterCode.getLanguage();
        this.code = starterCode.getCode();
    }

    public static CodingProblemStarterCodeResponse from(CodingProblemStarterCode starterCode) {
        return new CodingProblemStarterCodeResponse(starterCode);
    }
}
