package com.cobip.web.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.certificate.CertificateService;
import com.cobip.domain.user.MyPageService;
import com.cobip.domain.user.User;
import com.cobip.dto.mypage.LearningActivityHeartbeatRequest;
import com.cobip.dto.user.MyProfileUpdateRequest;
import com.cobip.dto.user.PasswordChangeRequest;
import com.cobip.dto.user.UserWithdrawalRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageService myPageService;

    @MockitoBean
    private CertificateService certificateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getProfileReturnsSuccessEnvelope() throws Exception {
        when(myPageService.getProfile(isNull(User.class))).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProfileAcceptsPatchRequest() throws Exception {
        when(myPageService.updateProfile(isNull(User.class), any(MyProfileUpdateRequest.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nickname": "updated",
                              "profileImageUrl": "https://example.com/profile.png"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changePasswordAcceptsPatchRequest() throws Exception {
        doNothing().when(myPageService).changePassword(isNull(User.class), any(PasswordChangeRequest.class));

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "currentPassword": "Password1!",
                              "newPassword": "NewPassword1!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void withdrawAcceptsDeleteRequest() throws Exception {
        doNothing().when(myPageService).withdraw(isNull(User.class), any(UserWithdrawalRequest.class));

        mockMvc.perform(delete("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "currentPassword": "Password1!",
                              "reason": "no longer needed"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void myPageCollectionEndpointsReturnPagedResponse() throws Exception {
        when(myPageService.getMyTemplates(isNull(User.class), any(Pageable.class))).thenReturn(PageResponse.from(Page.empty()));
        when(myPageService.getFavoriteTemplates(isNull(User.class), any(Pageable.class))).thenReturn(PageResponse.from(Page.empty()));
        when(myPageService.getLearningProgress(isNull(User.class), any(Pageable.class))).thenReturn(PageResponse.from(Page.empty()));
        when(myPageService.getActivities(isNull(User.class), any(Pageable.class))).thenReturn(PageResponse.from(Page.empty()));
        when(certificateService.getMyCertificates(isNull(User.class), any(Pageable.class))).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/users/me/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/v1/users/me/favorite-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/v1/users/me/learning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/v1/users/me/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/v1/users/me/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void learningActivityHeartbeatAcceptsPostRequest() throws Exception {
        when(myPageService.recordLearningActivityHeartbeat(
                isNull(User.class),
                any(LearningActivityHeartbeatRequest.class)
        )).thenReturn(null);

        mockMvc.perform(post("/api/v1/users/me/learning-activities/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "templateId": 1,
                              "activeSeconds": 30
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void subscriptionEndpointReturnsSuccessEnvelope() throws Exception {
        when(myPageService.getSubscription(isNull(User.class))).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/me/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelSubscriptionAcceptsPatchRequest() throws Exception {
        when(myPageService.cancelSubscription(isNull(User.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/users/me/subscription/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void dashboardEndpointReturnsSuccessEnvelope() throws Exception {
        when(myPageService.getDashboard(isNull(User.class))).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/me/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
