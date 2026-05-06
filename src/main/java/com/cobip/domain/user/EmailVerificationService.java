package com.cobip.domain.user;

import java.security.SecureRandom;
import java.util.Locale;

import com.cobip.dto.auth.EmailVerificationConfirmRequest;
import com.cobip.dto.auth.EmailVerificationSendRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.mail.EmailService;
import com.cobip.infra.redis.RedisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final EmailService emailService;

    @Value("${app.auth.email-verification.code-expiration-millis}")
    private long codeExpirationMillis;

    @Value("${app.auth.email-verification.verified-expiration-millis}")
    private long verifiedExpirationMillis;

    public void sendCode(EmailVerificationSendRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String code = createCode();
        redisService.saveEmailVerificationCode(email, code, codeExpirationMillis);
        emailService.sendVerificationCode(email, code);
    }

    public void confirmCode(EmailVerificationConfirmRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (!redisService.matchesEmailVerificationCode(email, request.getCode())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        redisService.deleteEmailVerificationCode(email);
        redisService.saveVerifiedEmail(email, verifiedExpirationMillis);
    }

    public boolean isVerified(String email) {
        return redisService.isVerifiedEmail(normalizeEmail(email));
    }

    public void consumeVerifiedEmail(String email) {
        redisService.deleteVerifiedEmail(normalizeEmail(email));
    }

    private String createCode() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
