package com.cobip.domain.user;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.dto.user.UserSettingUpdateRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSetting extends BaseTimeEntity {

    private static final int DEFAULT_EDITOR_FONT_SIZE = 14;
    private static final int DEFAULT_EDITOR_TAB_SIZE = 4;
    private static final String DEFAULT_THEME = "SYSTEM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private boolean pushNotificationEnabled;

    @Column(nullable = false)
    private boolean emailNotificationEnabled;

    @Column(nullable = false)
    private int editorFontSize;

    @Column(nullable = false, length = 30)
    private String editorTheme;

    @Column(nullable = false)
    private int editorTabSize;

    @Column(nullable = false, length = 30)
    private String serviceTheme;

    @Column(nullable = false)
    private boolean autoLoginEnabled;

    public static UserSetting defaultFor(User user) {
        return UserSetting.builder()
                .user(user)
                .pushNotificationEnabled(true)
                .emailNotificationEnabled(true)
                .editorFontSize(DEFAULT_EDITOR_FONT_SIZE)
                .editorTheme(DEFAULT_THEME)
                .editorTabSize(DEFAULT_EDITOR_TAB_SIZE)
                .serviceTheme(DEFAULT_THEME)
                .autoLoginEnabled(false)
                .build();
    }

    public void update(UserSettingUpdateRequest request) {
        if (request.getPushNotificationEnabled() != null) {
            this.pushNotificationEnabled = request.getPushNotificationEnabled();
        }
        if (request.getEmailNotificationEnabled() != null) {
            this.emailNotificationEnabled = request.getEmailNotificationEnabled();
        }
        if (request.getEditorFontSize() != null) {
            this.editorFontSize = request.getEditorFontSize();
        }
        if (request.getEditorTheme() != null) {
            this.editorTheme = request.getEditorTheme();
        }
        if (request.getEditorTabSize() != null) {
            this.editorTabSize = request.getEditorTabSize();
        }
        if (request.getServiceTheme() != null) {
            this.serviceTheme = request.getServiceTheme();
        }
        if (request.getAutoLoginEnabled() != null) {
            this.autoLoginEnabled = request.getAutoLoginEnabled();
        }
    }
}
