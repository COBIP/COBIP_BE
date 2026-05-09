package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.dto.auth.PasswordResetConfirmRequest;
import com.cobip.dto.auth.PasswordResetRequest;
import com.cobip.dto.auth.PasswordResetSendRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.mail.EmailService;
import com.cobip.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                redisService,
                emailService,
                passwordEncoder
        );
        ReflectionTestUtils.setField(passwordResetService, "codeExpirationMillis", 300_000L);
        ReflectionTestUtils.setField(passwordResetService, "verifiedExpirationMillis", 1_800_000L);
    }

    @Test
    void sendCodeStoresCodeAndSendsEmail() {
        PasswordResetSendRequest request = new PasswordResetSendRequest();
        ReflectionTestUtils.setField(request, "email", "USER@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        passwordResetService.sendCode(request);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisService).savePasswordResetCode(eq("user@example.com"), codeCaptor.capture(), eq(300_000L));
        verify(emailService).sendPasswordResetCode("user@example.com", codeCaptor.getValue());
        assertThat(codeCaptor.getValue()).matches("\\d{6}");
    }

    @Test
    void sendCodeRejectsUnknownEmail() {
        PasswordResetSendRequest request = new PasswordResetSendRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");

        assertThatThrownBy(() -> passwordResetService.sendCode(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void sendCodeRejectsSuspendedUser() {
        PasswordResetSendRequest request = new PasswordResetSendRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.SUSPENDED)));

        assertThatThrownBy(() -> passwordResetService.sendCode(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_DISABLED);
    }

    @Test
    void confirmCodeMarksEmailAsPasswordResetVerified() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        ReflectionTestUtils.setField(request, "email", "USER@example.com");
        ReflectionTestUtils.setField(request, "code", "123456");
        when(redisService.matchesPasswordResetCode("user@example.com", "123456")).thenReturn(true);

        passwordResetService.confirmCode(request);

        verify(redisService).deletePasswordResetCode("user@example.com");
        verify(redisService).savePasswordResetVerifiedEmail("user@example.com", 1_800_000L);
    }

    @Test
    void confirmCodeRejectsInvalidCode() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "code", "123456");

        assertThatThrownBy(() -> passwordResetService.confirmCode(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_FAILED);
    }

    @Test
    void resetPasswordChangesPasswordAndDeletesStoredTokens() {
        PasswordResetRequest request = new PasswordResetRequest();
        ReflectionTestUtils.setField(request, "email", "USER@example.com");
        ReflectionTestUtils.setField(request, "password", "Password1!");
        ReflectionTestUtils.setField(request, "confirmPassword", "Password1!");
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(redisService.isPasswordResetVerifiedEmail("user@example.com")).thenReturn(true);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-new-password");

        passwordResetService.resetPassword(request);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(redisService).deletePasswordResetVerifiedEmail("user@example.com");
        verify(redisService).deleteRefreshToken(1L);
    }

    @Test
    void resetPasswordRejectsUnverifiedEmail() {
        PasswordResetRequest request = new PasswordResetRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "password", "Password1!");
        ReflectionTestUtils.setField(request, "confirmPassword", "Password1!");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_VERIFICATION_REQUIRED);
    }

    @Test
    void resetPasswordRejectsPasswordMismatch() {
        PasswordResetRequest request = new PasswordResetRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "password", "Password1!");
        ReflectionTestUtils.setField(request, "confirmPassword", "Password2!");

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private User user(UserStatus status) {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .emailVerified(true)
                .build();
    }
}
