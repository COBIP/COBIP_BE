package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.dto.admin.AdminUserCreateRequest;
import com.cobip.dto.admin.AdminUserRoleUpdateRequest;
import com.cobip.dto.admin.AdminUserStatusUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ActivityHistoryService activityHistoryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(
                userRepository,
                redisService,
                activityHistoryService,
                passwordEncoder
        );
    }

    @Test
    void createAdminCreatesActiveAdminAccount() {
        User adminUser = user(2L, UserRole.ADMIN, UserStatus.ACTIVE);
        AdminUserCreateRequest request = createRequest("admin@example.com", "Password1!", "admin");
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("admin")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminUserService.createAdmin(request, adminUser);

        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.isEmailVerified()).isTrue();
        verify(activityHistoryService).record(
                eq(adminUser),
                eq(ActivityType.ADMIN_USER_ROLE_CHANGED),
                eq("Admin created admin account."),
                eq("USER"),
                org.mockito.ArgumentMatchers.isNull()
        );
    }

    @Test
    void changeStatusSuspendsUserAndDeletesRefreshToken() {
        User targetUser = user(1L, UserRole.USER, UserStatus.ACTIVE);
        User adminUser = user(2L, UserRole.ADMIN, UserStatus.ACTIVE);
        AdminUserStatusUpdateRequest request = statusRequest(UserStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        adminUserService.changeStatus(1L, request, adminUser);

        assertThat(targetUser.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(redisService).deleteRefreshToken(1L);
        verify(activityHistoryService).record(
                eq(adminUser),
                eq(ActivityType.ADMIN_USER_STATUS_CHANGED),
                eq("Admin changed user status to SUSPENDED"),
                eq("USER"),
                eq(1L)
        );
    }

    @Test
    void changeRoleUpdatesRoleAndDeletesRefreshToken() {
        User targetUser = user(1L, UserRole.USER, UserStatus.ACTIVE);
        User adminUser = user(2L, UserRole.ADMIN, UserStatus.ACTIVE);
        AdminUserRoleUpdateRequest request = roleRequest(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        adminUserService.changeRole(1L, request, adminUser);

        assertThat(targetUser.getRole()).isEqualTo(UserRole.ADMIN);
        verify(redisService).deleteRefreshToken(1L);
        verify(activityHistoryService).record(
                eq(adminUser),
                eq(ActivityType.ADMIN_USER_ROLE_CHANGED),
                eq("Admin changed user role to ADMIN"),
                eq("USER"),
                eq(1L)
        );
    }

    @Test
    void createAdminRejectsDuplicateEmail() {
        AdminUserCreateRequest request = createRequest("admin@example.com", "Password1!", "admin");
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createAdmin(request, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void changeStatusRejectsMissingUser() {
        AdminUserStatusUpdateRequest request = statusRequest(UserStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changeStatus(1L, request, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private AdminUserStatusUpdateRequest statusRequest(UserStatus status) {
        AdminUserStatusUpdateRequest request = new AdminUserStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    private AdminUserRoleUpdateRequest roleRequest(UserRole role) {
        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        ReflectionTestUtils.setField(request, "role", role);
        return request;
    }

    private AdminUserCreateRequest createRequest(String email, String password, String nickname) {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        return request;
    }

    private User user(Long id, UserRole role, UserStatus status) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("password")
                .nickname("user" + id)
                .role(role)
                .status(status)
                .emailVerified(true)
                .build();
    }
}
