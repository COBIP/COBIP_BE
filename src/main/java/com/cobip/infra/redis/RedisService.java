package com.cobip.infra.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";
    private static final String EMAIL_VERIFICATION_CODE_PREFIX = "email-verification-code:";
    private static final String EMAIL_VERIFIED_PREFIX = "email-verified:";
    private static final String PASSWORD_RESET_CODE_PREFIX = "password-reset-code:";
    private static final String PASSWORD_RESET_VERIFIED_PREFIX = "password-reset-verified:";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(refreshTokenKey(userId), refreshToken, Duration.ofMillis(expirationMillis));
    }

    public boolean matchesRefreshToken(Long userId, String refreshToken) {
        String storedToken = stringRedisTemplate.opsForValue().get(refreshTokenKey(userId));
        return refreshToken.equals(storedToken);
    }

    public void deleteRefreshToken(Long userId) {
        stringRedisTemplate.delete(refreshTokenKey(userId));
    }

    public void saveEmailVerificationCode(String email, String code, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(emailVerificationCodeKey(email), code, Duration.ofMillis(expirationMillis));
    }

    public boolean matchesEmailVerificationCode(String email, String code) {
        String storedCode = stringRedisTemplate.opsForValue().get(emailVerificationCodeKey(email));
        return code.equals(storedCode);
    }

    public void saveVerifiedEmail(String email, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(emailVerifiedKey(email), "true", Duration.ofMillis(expirationMillis));
    }

    public boolean isVerifiedEmail(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(emailVerifiedKey(email)));
    }

    public void deleteEmailVerificationCode(String email) {
        stringRedisTemplate.delete(emailVerificationCodeKey(email));
    }

    public void deleteVerifiedEmail(String email) {
        stringRedisTemplate.delete(emailVerifiedKey(email));
    }

    public void savePasswordResetCode(String email, String code, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(passwordResetCodeKey(email), code, Duration.ofMillis(expirationMillis));
    }

    public boolean matchesPasswordResetCode(String email, String code) {
        String storedCode = stringRedisTemplate.opsForValue().get(passwordResetCodeKey(email));
        return code.equals(storedCode);
    }

    public void deletePasswordResetCode(String email) {
        stringRedisTemplate.delete(passwordResetCodeKey(email));
    }

    public void savePasswordResetVerifiedEmail(String email, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(passwordResetVerifiedKey(email), "true", Duration.ofMillis(expirationMillis));
    }

    public boolean isPasswordResetVerifiedEmail(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(passwordResetVerifiedKey(email)));
    }

    public void deletePasswordResetVerifiedEmail(String email) {
        stringRedisTemplate.delete(passwordResetVerifiedKey(email));
    }

    private String refreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    private String emailVerificationCodeKey(String email) {
        return EMAIL_VERIFICATION_CODE_PREFIX + email;
    }

    private String emailVerifiedKey(String email) {
        return EMAIL_VERIFIED_PREFIX + email;
    }

    private String passwordResetCodeKey(String email) {
        return PASSWORD_RESET_CODE_PREFIX + email;
    }

    private String passwordResetVerifiedKey(String email) {
        return PASSWORD_RESET_VERIFIED_PREFIX + email;
    }
}
