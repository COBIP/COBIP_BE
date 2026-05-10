package com.cobip.domain.oauth;

import java.util.Locale;
import java.util.UUID;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.auth.AuthResponse;
import com.cobip.dto.auth.OAuthLoginRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.global.jwt.JwtProvider;
import com.cobip.infra.redis.RedisService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthAuthService {

    private static final int MAX_NICKNAME_LENGTH = 60;

    private final OAuthUserInfoClient oAuthUserInfoClient;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Transactional
    public AuthResponse login(OAuthLoginRequest request) {
        OAuthUserInfo userInfo = oAuthUserInfoClient.getUserInfo(request.getProvider(), request.getAccessToken());
        OAuthAccount account = oAuthAccountRepository
                .findByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
                .orElseGet(() -> createAccount(userInfo, request.getNickname()));

        User user = account.getUser();
        if (!user.isActiveAccount()) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }
        return issueTokens(user);
    }

    private OAuthAccount createAccount(OAuthUserInfo userInfo, String requestedNickname) {
        if (userInfo.email() == null || userInfo.email().isBlank()) {
            throw new CustomException(ErrorCode.OAUTH_EMAIL_REQUIRED);
        }
        if (userRepository.existsByEmail(userInfo.email())) {
            throw new CustomException(ErrorCode.OAUTH_ACCOUNT_NOT_LINKED);
        }

        try {
            User user = userRepository.save(User.builder()
                    .email(userInfo.email())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .nickname(uniqueNickname(requestedNickname, userInfo))
                    .profileImageUrl(userInfo.profileImageUrl())
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build());

            return oAuthAccountRepository.save(OAuthAccount.builder()
                    .user(user)
                    .provider(userInfo.provider())
                    .providerUserId(userInfo.providerUserId())
                    .email(userInfo.email())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.OAUTH_ACCOUNT_NOT_LINKED, e);
        }
    }

    private String uniqueNickname(String requestedNickname, OAuthUserInfo userInfo) {
        String baseNickname = nicknameBase(requestedNickname, userInfo);
        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }

        String providerSuffix = userInfo.providerUserId().replaceAll("[^A-Za-z0-9]", "");
        if (providerSuffix.length() > 8) {
            providerSuffix = providerSuffix.substring(providerSuffix.length() - 8);
        }
        if (providerSuffix.isBlank()) {
            providerSuffix = UUID.randomUUID().toString().substring(0, 8);
        }

        String suffix = "_" + providerSuffix.toLowerCase(Locale.ROOT);
        String candidate = truncate(baseNickname, MAX_NICKNAME_LENGTH - suffix.length()) + suffix;
        if (!userRepository.existsByNickname(candidate)) {
            return candidate;
        }

        throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    private String nicknameBase(String requestedNickname, OAuthUserInfo userInfo) {
        String rawNickname = firstNonBlank(requestedNickname, userInfo.nickname(), emailLocalPart(userInfo.email()));
        if (rawNickname == null) {
            rawNickname = userInfo.provider().name().toLowerCase(Locale.ROOT) + "_user";
        }
        return truncate(rawNickname.trim(), MAX_NICKNAME_LENGTH);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String emailLocalPart(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex <= 0 ? email : email.substring(0, atIndex);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());
        redisService.saveRefreshToken(user.getId(), refreshToken, jwtProvider.getRefreshTokenExpiration());
        return new AuthResponse(accessToken, refreshToken, user.getRole());
    }
}
