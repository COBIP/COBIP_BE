package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.dto.user.UserSettingUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserSettingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingRepository userSettingRepository;

    private UserSettingService userSettingService;

    @BeforeEach
    void setUp() {
        userSettingService = new UserSettingService(userRepository, userSettingRepository);
    }

    @Test
    void getSettingsCreatesDefaultSettingWhenMissing() {
        User user = user(1L);
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = userSettingService.getSettings(user);

        assertThat(response.isPushNotificationEnabled()).isTrue();
        assertThat(response.isEmailNotificationEnabled()).isTrue();
        assertThat(response.getEditorFontSize()).isEqualTo(14);
        assertThat(response.getEditorTabSize()).isEqualTo(4);
        assertThat(response.getServiceTheme()).isEqualTo("SYSTEM");
    }

    @Test
    void updateSettingsChangesProvidedFieldsOnly() {
        User user = user(1L);
        UserSetting setting = UserSetting.defaultFor(user);
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        var response = userSettingService.updateSettings(user, updateRequest());

        assertThat(response.isPushNotificationEnabled()).isFalse();
        assertThat(response.getEditorFontSize()).isEqualTo(18);
        assertThat(response.getServiceTheme()).isEqualTo("DARK");
        assertThat(response.isEmailNotificationEnabled()).isTrue();
    }

    @Test
    void getSettingsRejectsAnonymousUser() {
        assertThatThrownBy(() -> userSettingService.getSettings(null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_REQUIRED);
    }

    private UserSettingUpdateRequest updateRequest() {
        UserSettingUpdateRequest request = new UserSettingUpdateRequest();
        ReflectionTestUtils.setField(request, "pushNotificationEnabled", false);
        ReflectionTestUtils.setField(request, "editorFontSize", 18);
        ReflectionTestUtils.setField(request, "serviceTheme", "DARK");
        return request;
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
