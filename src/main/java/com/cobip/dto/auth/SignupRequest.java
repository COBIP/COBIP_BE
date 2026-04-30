package com.cobip.dto.auth;

import lombok.Getter;

@Getter
public class SignupRequest {

    private String email;

    private String password;

    private String confirmPassword; // 비밀번호 재입력 확인용

    private String nickname;
}