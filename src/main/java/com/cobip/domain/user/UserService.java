package com.cobip.domain.user;

import com.cobip.dto.auth.AuthResponse;
import com.cobip.dto.auth.LoginRequest;
import com.cobip.dto.auth.RefreshTokenRequest;
import com.cobip.dto.auth.SignupRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.global.jwt.JwtProvider;
import com.cobip.infra.redis.RedisService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        try {
            User user = userRepository.save(User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build());
            AuthResponse response = issueTokens(user);
            emailVerificationService.consumeVerifiedEmail(request.getEmail());
            return response;
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL, e);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isActiveAccount()) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!user.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse reissue(RefreshTokenRequest request) {
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        Long userId = jwtProvider.getUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActiveAccount()) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!redisService.matchesRefreshToken(userId, request.getRefreshToken())) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        return issueTokens(user);
    }

    public void logout(User user) {
        redisService.deleteRefreshToken(user.getId());
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    private void validateSignupRequest(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (!emailVerificationService.isVerified(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        // Refresh Token은 로그아웃 또는 만료 시 삭제할 수 있도록 Redis에 저장한다.
        redisService.saveRefreshToken(user.getId(), refreshToken, jwtProvider.getRefreshTokenExpiration());

        return new AuthResponse(accessToken, refreshToken);
    }
}
