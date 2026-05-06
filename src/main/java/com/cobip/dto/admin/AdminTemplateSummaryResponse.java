package com.cobip.dto.admin;

import java.time.LocalDateTime;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import lombok.Getter;

@Getter
public class AdminTemplateSummaryResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final TemplateDifficulty difficulty;
    private final TemplateVisibility visibility;
    private final TemplateAccessLevel accessLevel;
    private final long viewCount;
    private final long favoriteCount;
    private final Long ownerId;
    private final String ownerNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminTemplateSummaryResponse(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.visibility = template.getVisibility();
        this.accessLevel = template.getAccessLevel();
        this.viewCount = template.getViewCount();
        this.favoriteCount = template.getFavoriteCount();
        this.ownerId = template.getOwner().getId();
        this.ownerNickname = template.getOwner().getNickname();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }

    public static AdminTemplateSummaryResponse from(Template template) {
        return new AdminTemplateSummaryResponse(template);
    }
}
