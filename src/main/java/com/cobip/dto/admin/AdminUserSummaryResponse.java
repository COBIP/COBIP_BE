package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;

import lombok.Getter;

@Getter
public class AdminUserSummaryResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final UserRole role;
    private final UserStatus status;
    private final boolean emailVerified;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminUserSummaryResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.emailVerified = user.isEmailVerified();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public static AdminUserSummaryResponse from(User user) {
        return new AdminUserSummaryResponse(user);
    }
}
