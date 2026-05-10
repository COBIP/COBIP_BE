package com.cobip.dto.admin;

import com.cobip.domain.user.UserRole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserRoleUpdateRequest {

    @NotNull(message = "Role is required.")
    private UserRole role;
}
