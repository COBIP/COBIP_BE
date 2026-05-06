package com.cobip.dto.admin;

import com.cobip.domain.moderation.ContentModerationAction;
import com.cobip.domain.moderation.ContentModerationTargetType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminContentModerationRequest {

    @NotNull(message = "targetType is required.")
    private ContentModerationTargetType targetType;

    @NotNull(message = "targetId is required.")
    private Long targetId;

    @NotNull(message = "action is required.")
    private ContentModerationAction action;

    @Size(max = 2000, message = "adminMemo must be 2000 characters or fewer.")
    private String adminMemo;
}
