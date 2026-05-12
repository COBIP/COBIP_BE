package com.cobip.web.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.oauth.OAuthAuthService;
import com.cobip.domain.user.EmailVerificationService;
import com.cobip.domain.user.PasswordResetService;
import com.cobip.domain.user.UserService;
import com.cobip.global.config.CorsConfig;
import com.cobip.global.config.SecurityConfig;
import com.cobip.global.security.JwtAccessDeniedHandler;
import com.cobip.global.security.JwtAuthenticationEntryPoint;
import com.cobip.global.security.JwtAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

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
    CorsConfig.class
})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000,https://cobip.tech")
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
    private OAuthAuthService oAuthAuthService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(jwtAuthenticationEntryPoint).commence(any(), any(), any());
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
    void practiceProjectRunPreflightAllowsProductionOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/templates/1/practice/missions/1/project/run")
                .header(HttpHeaders.ORIGIN, "https://cobip.tech")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://cobip.tech"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")));
    }

    @Test
    void unauthorizedPracticeProjectRunResponseIncludesCorsHeader() throws Exception {
        mockMvc.perform(post("/api/v1/templates/1/practice/missions/1/project/run")
                .header(HttpHeaders.ORIGIN, "https://cobip.tech")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://cobip.tech"));
    }

    @Test
    void protectedEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }
}
