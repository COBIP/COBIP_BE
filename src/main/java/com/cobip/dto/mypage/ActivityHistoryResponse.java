package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.cobip.domain.activity.ActivityHistory;
import com.cobip.domain.activity.ActivityType;

import lombok.Getter;

@Getter
public class ActivityHistoryResponse {

    private final Long id;
    private final ActivityType type;
    private final String message;
    private final String targetType;
    private final Long targetId;
    private final LocalDateTime createdAt;

    private ActivityHistoryResponse(ActivityHistory activityHistory) {
        this.id = activityHistory.getId();
        this.type = activityHistory.getType();
        this.message = activityHistory.getMessage();
        this.targetType = activityHistory.getTargetType();
        this.targetId = activityHistory.getTargetId();
        this.createdAt = activityHistory.getCreatedAt();
    }

    public static ActivityHistoryResponse from(ActivityHistory activityHistory) {
        return new ActivityHistoryResponse(activityHistory);
    }
}
