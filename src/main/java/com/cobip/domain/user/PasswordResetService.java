package com.cobip.domain.user;

import java.security.SecureRandom;
import java.util.Locale;

import com.cobip.dto.auth.PasswordResetConfirmRequest;
import com.cobip.dto.auth.PasswordResetRequest;
import com.cobip.dto.auth.PasswordResetSendRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.mail.EmailService;
import com.cobip.infra.redis.RedisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.password-reset.code-expiration-millis}")
    private long codeExpirationMillis;

    @Value("${app.auth.password-reset.verified-expiration-millis}")
    private long verifiedExpirationMillis;

    @Transactional(readOnly = true)
    public void sendCode(PasswordResetSendRequest request) {
        User user = findActiveUserByEmail(normalizeEmail(request.getEmail()));

        String code = createCode();
        redisService.savePasswordResetCode(user.getEmail(), code, codeExpirationMillis);
        emailService.sendPasswordResetCode(user.getEmail(), code);
    }

    public void confirmCode(PasswordResetConfirmRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (!redisService.matchesPasswordResetCode(email, request.getCode())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        redisService.deletePasswordResetCode(email);
        redisService.savePasswordResetVerifiedEmail(email, verifiedExpirationMillis);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        validatePasswordConfirmation(request);
        String email = normalizeEmail(request.getEmail());
        User user = findActiveUserByEmail(email);

        if (!redisService.isPasswordResetVerifiedEmail(email)) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_VERIFICATION_REQUIRED);
        }

        user.changePassword(passwordEncoder.encode(request.getPassword()));
        redisService.deletePasswordResetVerifiedEmail(email);
        redisService.deleteRefreshToken(user.getId());
    }

    private void validatePasswordConfirmation(PasswordResetRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private User findActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActiveAccount()) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }
        return user;
    }

    private String createCode() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
