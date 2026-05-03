package com.cobip.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyProfileUpdateRequest {

    @Size(max = 60, message = "닉네임은 60자 이하로 입력해야 합니다.")
    private String nickname;

    @Size(max = 1000, message = "프로필 이미지 URL은 1000자 이하로 입력해야 합니다.")
    private String profileImageUrl;
}
