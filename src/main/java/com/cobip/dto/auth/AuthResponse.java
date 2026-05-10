package com.cobip.dto.auth;

import com.cobip.domain.user.UserRole;

import lombok.Getter;

@Getter
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final UserRole role;

    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, null);
    }

    public AuthResponse(String accessToken, String refreshToken, UserRole role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.role = role;
    }
}
