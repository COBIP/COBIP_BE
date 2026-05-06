package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.dto.admin.AdminUserStatusUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ActivityHistoryService activityHistoryService;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, redisService, activityHistoryService);
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
