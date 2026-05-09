package com.cobip.web.auth;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.user.EmailVerificationService;
import com.cobip.domain.user.PasswordResetService;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserService;
import com.cobip.global.config.CorsConfig;
import com.cobip.global.config.SecurityConfig;
import com.cobip.global.jwt.JwtProvider;
import com.cobip.global.security.JwtAccessDeniedHandler;
import com.cobip.global.security.JwtAuthenticationEntryPoint;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({
    SecurityConfig.class,
    CorsConfig.class,
    JwtAuthenticationFilter.class,
    JwtAuthenticationEntryPoint.class,
    JwtAccessDeniedHandler.class
})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class AuthSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        when(jwtProvider.getAuthorizationHeader()).thenReturn(HttpHeaders.AUTHORIZATION);
        when(jwtProvider.resolveToken(isNull())).thenReturn(null);
    }

    @Test
    void emailVerificationDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-verifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "user@example.com"
                    }
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void emailVerificationAliasDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "user@example.com"
                    }
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void emailVerificationTrailingSlashDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-verifications/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "user@example.com"
                    }
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void emailVerificationPreflightDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(options("/api/v1/auth/email-verifications")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }
}
