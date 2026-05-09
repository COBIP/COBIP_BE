package com.cobip.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.user.User;
import com.cobip.domain.user.EmailVerificationService;
import com.cobip.domain.oauth.OAuthAuthService;
import com.cobip.domain.user.PasswordResetService;
import com.cobip.domain.user.UserService;
import com.cobip.dto.auth.AuthResponse;
import com.cobip.dto.auth.EmailVerificationConfirmRequest;
import com.cobip.dto.auth.EmailVerificationSendRequest;
import com.cobip.dto.auth.LoginRequest;
import com.cobip.dto.auth.OAuthLoginRequest;
import com.cobip.dto.auth.PasswordResetConfirmRequest;
import com.cobip.dto.auth.PasswordResetRequest;
import com.cobip.dto.auth.PasswordResetSendRequest;
import com.cobip.dto.auth.RefreshTokenRequest;
import com.cobip.dto.auth.SignupRequest;
import com.cobip.global.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private OAuthAuthService oAuthAuthService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void checkEmailAvailabilityReturnsAvailabilityResponse() throws Exception {
        when(userService.isEmailAvailable("user@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/email/availability")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    void checkNicknameAvailabilityReturnsAvailabilityResponse() throws Exception {
        when(userService.isNicknameAvailable("cobip")).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/nickname/availability")
                        .param("nickname", "cobip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(false));
    }

    @Test
    void checkEmailAvailabilityRejectsInvalidEmail() throws Exception {
        mockMvc.perform(get("/api/v1/auth/email/availability")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void sendEmailVerificationReturnsSuccessResponse() throws Exception {
        doNothing().when(emailVerificationService).sendCode(any(EmailVerificationSendRequest.class));

        mockMvc.perform(post("/api/v1/auth/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이메일 인증 코드가 발송되었습니다."));
    }

    @Test
    void confirmEmailVerificationReturnsSuccessResponse() throws Exception {
        doNothing().when(emailVerificationService).confirmCode(any(EmailVerificationConfirmRequest.class));

        mockMvc.perform(post("/api/v1/auth/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com",
                              "code": "123456"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    void sendPasswordResetVerificationReturnsSuccessResponse() throws Exception {
        doNothing().when(passwordResetService).sendCode(any(PasswordResetSendRequest.class));

        mockMvc.perform(post("/api/v1/auth/password-reset/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void confirmPasswordResetVerificationReturnsSuccessResponse() throws Exception {
        doNothing().when(passwordResetService).confirmCode(any(PasswordResetConfirmRequest.class));

        mockMvc.perform(post("/api/v1/auth/password-reset/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com",
                              "code": "123456"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPasswordReturnsSuccessResponse() throws Exception {
        doNothing().when(passwordResetService).resetPassword(any(PasswordResetRequest.class));

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com",
                              "password": "Password1!",
                              "confirmPassword": "Password1!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signupReturnsTokenResponse() throws Exception {
        when(userService.signup(any(SignupRequest.class)))
                .thenReturn(new AuthResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com",
                              "password": "Password1!",
                              "confirmPassword": "Password1!",
                              "nickname": "cobip"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void loginReturnsTokenResponse() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "user@example.com",
                              "password": "Password1!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void oauthLoginReturnsTokenResponse() throws Exception {
        when(oAuthAuthService.login(any(OAuthLoginRequest.class)))
                .thenReturn(new AuthResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "provider": "GOOGLE",
                              "accessToken": "provider-access-token",
                              "nickname": "cobip"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void oauthLoginRejectsMissingAccessToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "provider": "GOOGLE"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void reissueReturnsTokenResponse() throws Exception {
        when(userService.reissue(any(RefreshTokenRequest.class)))
                .thenReturn(new AuthResponse("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TestRefreshTokenRequest("refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void logoutReturnsSuccessResponse() throws Exception {
        doNothing().when(userService).logout(isNull(User.class));

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private record TestRefreshTokenRequest(String refreshToken) {
    }
}
