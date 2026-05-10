package com.cobip.dto.admin;

import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingProblemStarterCode;

import lombok.Getter;

@Getter
public class AdminCodingProblemStarterCodeResponse {

    private final Long id;
    private final CodingLanguage language;
    private final String code;

    private AdminCodingProblemStarterCodeResponse(CodingProblemStarterCode starterCode) {
        this.id = starterCode.getId();
        this.language = starterCode.getLanguage();
        this.code = starterCode.getCode();
    }

    public static AdminCodingProblemStarterCodeResponse from(CodingProblemStarterCode starterCode) {
        return new AdminCodingProblemStarterCodeResponse(starterCode);
    }
}
