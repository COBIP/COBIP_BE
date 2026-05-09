package com.cobip.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserSettingUpdateRequest {

    private Boolean pushNotificationEnabled;

    private Boolean emailNotificationEnabled;

    @Min(10)
    @Max(32)
    private Integer editorFontSize;

    @Pattern(regexp = "^(SYSTEM|LIGHT|DARK)$")
    private String editorTheme;

    @Min(2)
    @Max(8)
    private Integer editorTabSize;

    @Pattern(regexp = "^(SYSTEM|LIGHT|DARK)$")
    private String serviceTheme;

    private Boolean autoLoginEnabled;
}
