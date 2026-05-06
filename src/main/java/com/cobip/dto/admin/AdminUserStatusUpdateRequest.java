package com.cobip.dto.admin;

import com.cobip.domain.user.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserStatusUpdateRequest {

    @NotNull(message = "status is required.")
    private UserStatus status;
}
