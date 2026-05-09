package com.cobip.domain.user;

import com.cobip.dto.user.UserSettingResponse;
import com.cobip.dto.user.UserSettingUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;

    @Transactional
    public UserSettingResponse getSettings(User user) {
        return UserSettingResponse.from(getOrCreateSetting(user));
    }

    @Transactional
    public UserSettingResponse updateSettings(User user, UserSettingUpdateRequest request) {
        UserSetting setting = getOrCreateSetting(user);
        setting.update(request);
        return UserSettingResponse.from(setting);
    }

    private UserSetting getOrCreateSetting(User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        return userSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> userSettingRepository.save(UserSetting.defaultFor(getManagedUser(user))));
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
