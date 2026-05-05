package com.cobip.dto.user;

import java.time.LocalDateTime;

import com.cobip.domain.user.User;

import lombok.Getter;

@Getter
public class MyProfileResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final LocalDateTime createdAt;

    private MyProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.createdAt = user.getCreatedAt();
    }

    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(user);
    }
}
