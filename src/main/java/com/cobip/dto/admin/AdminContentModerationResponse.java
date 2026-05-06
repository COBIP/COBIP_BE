package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.moderation.ContentModerationAction;
import com.cobip.domain.moderation.ContentModerationTargetType;
import com.cobip.domain.user.User;

import lombok.Getter;

@Getter
public class AdminContentModerationResponse {

    private final ContentModerationTargetType targetType;
    private final Long targetId;
    private final ContentModerationAction action;
    private final String resultStatus;
    private final String adminMemo;
    private final Long moderatedById;
    private final String moderatedByNickname;
    private final LocalDateTime moderatedAt;

    private AdminContentModerationResponse(
        AdminContentModerationRequest request,
        String resultStatus,
        User adminUser,
        LocalDateTime moderatedAt
    ) {
        this.targetType = request.getTargetType();
        this.targetId = request.getTargetId();
        this.action = request.getAction();
        this.resultStatus = resultStatus;
        this.adminMemo = request.getAdminMemo();
        this.moderatedById = adminUser == null ? null : adminUser.getId();
        this.moderatedByNickname = adminUser == null ? null : adminUser.getNickname();
        this.moderatedAt = moderatedAt;
    }

    public static AdminContentModerationResponse of(
        AdminContentModerationRequest request,
        String resultStatus,
        User adminUser,
        LocalDateTime moderatedAt
    ) {
        return new AdminContentModerationResponse(request, resultStatus, adminUser, moderatedAt);
    }
}
