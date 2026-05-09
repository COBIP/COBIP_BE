package com.cobip.dto.user;

import com.cobip.domain.user.UserSetting;

import lombok.Getter;

@Getter
public class UserSettingResponse {

    private final boolean pushNotificationEnabled;
    private final boolean emailNotificationEnabled;
    private final int editorFontSize;
    private final String editorTheme;
    private final int editorTabSize;
    private final String serviceTheme;
    private final boolean autoLoginEnabled;

    private UserSettingResponse(UserSetting setting) {
        this.pushNotificationEnabled = setting.isPushNotificationEnabled();
        this.emailNotificationEnabled = setting.isEmailNotificationEnabled();
        this.editorFontSize = setting.getEditorFontSize();
        this.editorTheme = setting.getEditorTheme();
        this.editorTabSize = setting.getEditorTabSize();
        this.serviceTheme = setting.getServiceTheme();
        this.autoLoginEnabled = setting.isAutoLoginEnabled();
    }

    public static UserSettingResponse from(UserSetting setting) {
        return new UserSettingResponse(setting);
    }
}
