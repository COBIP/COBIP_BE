package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.user.AdminUserService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.admin.AdminUserCreateRequest;
import com.cobip.dto.admin.AdminUserStatusUpdateRequest;
import com.cobip.dto.admin.AdminUserRoleUpdateRequest;
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

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getUsersReturnsPagedResponse() throws Exception {
        when(adminUserService.getUsers(
                eq("user"),
                eq(UserRole.USER),
                eq(UserStatus.ACTIVE),
                eq(true),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("keyword", "user")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("emailVerified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getUserReturnsDetailResponseEnvelope() throws Exception {
        when(adminUserService.getUser(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createAdminAcceptsPostRequest() throws Exception {
        when(adminUserService.createAdmin(any(AdminUserCreateRequest.class), isNull(User.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/users/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "admin@example.com",
                              "password": "Password1!",
                              "nickname": "admin"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeStatusAcceptsPatchRequest() throws Exception {
        when(adminUserService.changeStatus(eq(1L), any(AdminUserStatusUpdateRequest.class), isNull(User.class)))
                .thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "SUSPENDED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeRoleAcceptsPatchRequest() throws Exception {
        when(adminUserService.changeRole(eq(1L), any(AdminUserRoleUpdateRequest.class), isNull(User.class)))
                .thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "role": "ADMIN"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeStatusRejectsMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
