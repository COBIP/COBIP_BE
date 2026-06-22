package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.dto.auth.AuthResponse;
import com.cobip.dto.auth.LoginRequest;
import com.cobip.dto.auth.RefreshTokenRequest;
import com.cobip.dto.auth.SignupRequest;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisService redisService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                emailVerificationService,
                passwordEncoder,
                jwtProvider,
                redisService
        );
    }

    @Test
    void loginRejectsSuspendedUser() {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "password", "Password1!");
        User user = user(UserStatus.SUSPENDED);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_DISABLED);
    }

    @Test
    void reissueRejectsDeletedUser() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        ReflectionTestUtils.setField(request, "refreshToken", "refresh-token");
        when(jwtProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtProvider.getUserId("refresh-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.DELETED)));

        assertThatThrownBy(() -> userService.reissue(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_DISABLED);
    }

    @Test
    void isEmailAvailableReturnsFalseWhenEmailExists() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        assertThat(userService.isEmailAvailable("user@example.com")).isFalse();
    }

    @Test
    void isNicknameAvailableReturnsTrueWhenNicknameDoesNotExist() {
        when(userRepository.existsByNickname("cobip")).thenReturn(false);

        assertThat(userService.isNicknameAvailable("cobip")).isTrue();
    }

    @Test
    void isEmailAvailableReturnsTrueWhenOnlyDeletedUserExists() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.DELETED)));

        assertThat(userService.isEmailAvailable("user@example.com")).isTrue();
    }

    @Test
    void signupReactivatesDeletedUser() {
        SignupRequest request = signupRequest("user@example.com", "Password1!", "rejoin-user");
        User deletedUser = user(UserStatus.DELETED);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(deletedUser));
        when(userRepository.existsByNicknameAndIdNot("rejoin-user", 1L)).thenReturn(false);
        when(emailVerificationService.isVerified("user@example.com")).thenReturn(true);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-new-password");
        stubTokens(1L, "user@example.com");

        AuthResponse response = userService.signup(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(deletedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(deletedUser.getNickname()).isEqualTo("rejoin-user");
        assertThat(deletedUser.getPassword()).isEqualTo("encoded-new-password");
        verify(userRepository).findByEmail("user@example.com");
        verify(emailVerificationService).consumeVerifiedEmail("user@example.com");
    }

    @Test
    void signupRejectsNicknameOwnedByAnotherUserWhenReactivatingDeletedUser() {
        SignupRequest request = signupRequest("user@example.com", "Password1!", "taken-nickname");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(UserStatus.DELETED)));
        when(userRepository.existsByNicknameAndIdNot("taken-nickname", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    void signupCreatesNewUserWhenEmailDoesNotExist() {
        SignupRequest request = signupRequest("new@example.com", "Password1!", "new-user");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("new-user")).thenReturn(false);
        when(emailVerificationService.isVerified("new@example.com")).thenReturn(true);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 2L);
            return user;
        });
        stubTokens(2L, "new@example.com");

        AuthResponse response = userService.signup(request);

        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(User.class));
    }

    private void stubTokens(Long userId, String email) {
        when(jwtProvider.createAccessToken(userId, email)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(userId, email)).thenReturn("refresh-token");
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(1200L);
    }

    private SignupRequest signupRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "confirmPassword", password);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        return request;
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
