package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.activity.ActivityHistory;
import com.cobip.domain.activity.ActivityType;

import lombok.Getter;

@Getter
public class AdminActivityHistoryResponse {

    private final Long id;
    private final Long userId;
    private final String userEmail;
    private final String userNickname;
    private final ActivityType type;
    private final String message;
    private final String targetType;
    private final Long targetId;
    private final LocalDateTime createdAt;

    private AdminActivityHistoryResponse(ActivityHistory activityHistory) {
        this.id = activityHistory.getId();
        this.userId = activityHistory.getUser().getId();
        this.userEmail = activityHistory.getUser().getEmail();
        this.userNickname = activityHistory.getUser().getNickname();
        this.type = activityHistory.getType();
        this.message = activityHistory.getMessage();
        this.targetType = activityHistory.getTargetType();
        this.targetId = activityHistory.getTargetId();
        this.createdAt = activityHistory.getCreatedAt();
    }

    public static AdminActivityHistoryResponse from(ActivityHistory activityHistory) {
        return new AdminActivityHistoryResponse(activityHistory);
    }
}
