package com.cobip.web.user;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserSettingService;
import com.cobip.dto.user.UserSettingResponse;
import com.cobip.dto.user.UserSettingUpdateRequest;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/settings")
public class UserSettingController {

    private final UserSettingService userSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingResponse>> getSettings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(userSettingService.getSettings(user)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateSettings(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid UserSettingUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("설정이 저장되었습니다.", userSettingService.updateSettings(user, request)));
    }
}
