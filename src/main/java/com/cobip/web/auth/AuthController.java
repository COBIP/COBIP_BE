package com.cobip.web.auth;

import com.cobip.domain.user.User;
import com.cobip.domain.user.EmailVerificationService;
import com.cobip.domain.user.PasswordResetService;
import com.cobip.domain.user.UserService;
import com.cobip.dto.auth.AuthResponse;
import com.cobip.dto.auth.EmailVerificationConfirmRequest;
import com.cobip.dto.auth.EmailVerificationSendRequest;
import com.cobip.dto.auth.LoginRequest;
import com.cobip.dto.auth.PasswordResetConfirmRequest;
import com.cobip.dto.auth.PasswordResetRequest;
import com.cobip.dto.auth.PasswordResetSendRequest;
import com.cobip.dto.auth.RefreshTokenRequest;
import com.cobip.dto.auth.SignupRequest;
import com.cobip.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/email-verifications")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(
        @RequestBody @Valid EmailVerificationSendRequest request
    ) {
        emailVerificationService.sendCode(request);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증 코드가 발송되었습니다.", null));
    }

    @PostMapping("/email-verifications/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmEmailVerification(
        @RequestBody @Valid EmailVerificationConfirmRequest request
    ) {
        emailVerificationService.confirmCode(request);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", null));
    }

    @PostMapping("/password-reset/email-verifications")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetVerification(
        @RequestBody @Valid PasswordResetSendRequest request
    ) {
        passwordResetService.sendCode(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 인증 코드가 발송되었습니다.", null));
    }

    @PostMapping("/password-reset/email-verifications/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordResetVerification(
        @RequestBody @Valid PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmCode(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 인증이 완료되었습니다.", null));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다.", null));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", userService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그인되었습니다.", userService.login(request)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthResponse>> reissue(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", userService.reissue(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        userService.logout(user);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }
}
