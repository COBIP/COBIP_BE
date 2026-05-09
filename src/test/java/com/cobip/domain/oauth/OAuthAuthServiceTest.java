package com.cobip.domain.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.auth.OAuthLoginRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.global.jwt.JwtProvider;
import com.cobip.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuthAuthServiceTest {

    @Mock
    private OAuthUserInfoClient oAuthUserInfoClient;

    @Mock
    private OAuthAccountRepository oAuthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisService redisService;

    private OAuthAuthService oAuthAuthService;

    @BeforeEach
    void setUp() {
        oAuthAuthService = new OAuthAuthService(
                oAuthUserInfoClient,
                oAuthAccountRepository,
                userRepository,
                passwordEncoder,
                jwtProvider,
                redisService
        );
    }

    @Test
    void loginIssuesTokensForLinkedOAuthAccount() {
        User user = user(1L, UserStatus.ACTIVE);
        OAuthLoginRequest request = request(OAuthProvider.GOOGLE, "google-token", null);
        OAuthUserInfo userInfo = userInfo(OAuthProvider.GOOGLE, "google-1", "user@example.com", "user");
        OAuthAccount account = account(user, userInfo);
        when(oAuthUserInfoClient.getUserInfo(OAuthProvider.GOOGLE, "google-token")).thenReturn(userInfo);
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-1"))
                .thenReturn(Optional.of(account));
        stubTokens(user.getId(), user.getEmail());

        var response = oAuthAuthService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(redisService).saveRefreshToken(1L, "refresh-token", 1200L);
    }

    @Test
    void loginCreatesUserAndOAuthAccountForNewSocialUser() {
        OAuthLoginRequest request = request(OAuthProvider.NAVER, "naver-token", "naver-user");
        OAuthUserInfo userInfo = userInfo(OAuthProvider.NAVER, "naver-1", "new@example.com", "provider-name");
        when(oAuthUserInfoClient.getUserInfo(OAuthProvider.NAVER, "naver-token")).thenReturn(userInfo);
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.NAVER, "naver-1"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("naver-user")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 2L);
            return user;
        });
        when(oAuthAccountRepository.save(any(OAuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubTokens(2L, "new@example.com");

        var response = oAuthAuthService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userRepository).save(any(User.class));
        verify(oAuthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    void loginRejectsExistingEmailWithoutLinkedOAuthAccount() {
        OAuthLoginRequest request = request(OAuthProvider.KAKAO, "kakao-token", null);
        OAuthUserInfo userInfo = userInfo(OAuthProvider.KAKAO, "kakao-1", "user@example.com", "kakao-user");
        when(oAuthUserInfoClient.getUserInfo(OAuthProvider.KAKAO, "kakao-token")).thenReturn(userInfo);
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "kakao-1"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> oAuthAuthService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OAUTH_ACCOUNT_NOT_LINKED);
    }

    @Test
    void loginRejectsProviderUserInfoWithoutEmail() {
        OAuthLoginRequest request = request(OAuthProvider.KAKAO, "kakao-token", null);
        OAuthUserInfo userInfo = userInfo(OAuthProvider.KAKAO, "kakao-1", null, "kakao-user");
        when(oAuthUserInfoClient.getUserInfo(OAuthProvider.KAKAO, "kakao-token")).thenReturn(userInfo);
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "kakao-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> oAuthAuthService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OAUTH_EMAIL_REQUIRED);
    }

    private void stubTokens(Long userId, String email) {
        when(jwtProvider.createAccessToken(userId, email)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(userId, email)).thenReturn("refresh-token");
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(1200L);
    }

    private OAuthLoginRequest request(OAuthProvider provider, String accessToken, String nickname) {
        OAuthLoginRequest request = new OAuthLoginRequest();
        ReflectionTestUtils.setField(request, "provider", provider);
        ReflectionTestUtils.setField(request, "accessToken", accessToken);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        return request;
    }

    private OAuthUserInfo userInfo(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname
    ) {
        return new OAuthUserInfo(provider, providerUserId, email, nickname, "https://example.com/profile.png");
    }

    private OAuthAccount account(User user, OAuthUserInfo userInfo) {
        return OAuthAccount.builder()
                .id(1L)
                .user(user)
                .provider(userInfo.provider())
                .providerUserId(userInfo.providerUserId())
                .email(userInfo.email())
                .build();
    }

    private User user(Long id, UserStatus status) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .emailVerified(true)
                .build();
    }
}
