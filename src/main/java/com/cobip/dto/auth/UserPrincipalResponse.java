package com.cobip.dto.auth;

import com.cobip.domain.user.User;

import lombok.Getter;

@Getter
public class UserPrincipalResponse {

    private final Long id;
    private final String email;
    private final String nickname;

    private UserPrincipalResponse(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserPrincipalResponse from(User user) {
        return new UserPrincipalResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
