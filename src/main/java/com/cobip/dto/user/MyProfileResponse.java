package com.cobip.dto.user;

import java.time.LocalDateTime;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;

import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final UserRole role;
    private final LocalDateTime createdAt;

    private MyProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }

    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(user);
    }
}
