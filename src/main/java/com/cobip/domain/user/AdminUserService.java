package com.cobip.domain.user;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.dto.admin.AdminUserDetailResponse;
import com.cobip.dto.admin.AdminUserStatusUpdateRequest;
import com.cobip.dto.admin.AdminUserSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.redis.RedisService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ActivityHistoryService activityHistoryService;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> getUsers(
        String keyword,
        UserRole role,
        UserStatus status,
        Boolean emailVerified,
        Pageable pageable
    ) {
        return PageResponse.from(userRepository.findAll(
                userSpec(keyword, role, status, emailVerified),
                pageable
        ).map(AdminUserSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        return AdminUserDetailResponse.from(findUser(userId));
    }

    @Transactional
    public AdminUserDetailResponse changeStatus(Long userId, AdminUserStatusUpdateRequest request, User adminUser) {
        User user = findUser(userId);
        user.changeStatus(request.getStatus());

        if (!user.isActiveAccount()) {
            redisService.deleteRefreshToken(user.getId());
        }
        if (adminUser != null) {
            activityHistoryService.record(
                    adminUser,
                    ActivityType.ADMIN_USER_STATUS_CHANGED,
                    "Admin changed user status to " + request.getStatus(),
                    "USER",
                    user.getId()
            );
        }

        return AdminUserDetailResponse.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Specification<User> userSpec(
        String keyword,
        UserRole role,
        UserStatus status,
        Boolean emailVerified
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nickname")), likeKeyword)
                ));
            }
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (emailVerified != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), emailVerified));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
