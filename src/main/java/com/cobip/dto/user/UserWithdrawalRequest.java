package com.cobip.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserWithdrawalRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "탈퇴 사유는 필수입니다.")
    @Size(max = 500, message = "탈퇴 사유는 500자 이하로 입력해야 합니다.")
    private String reason;
}
