package com.cobip.dto.template;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;

import lombok.Getter;

@Getter
public class TemplateSummaryResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final TemplateDifficulty difficulty;
    private final TemplateAccessLevel accessLevel;
    private final List<String> techStacks;
    private final String thumbnailUrl;
    private final long viewCount;
    private final long favoriteCount;
    private final Long ownerId;
    private final String ownerNickname;
    private final LocalDateTime createdAt;

    private TemplateSummaryResponse(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.description = template.getDescription();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.accessLevel = template.getAccessLevel();
        this.techStacks = List.copyOf(template.getTechStacks());
        this.thumbnailUrl = template.getThumbnailUrl();
        this.viewCount = template.getViewCount();
        this.favoriteCount = template.getFavoriteCount();
        this.ownerId = template.getOwner().getId();
        this.ownerNickname = template.getOwner().getNickname();
        this.createdAt = template.getCreatedAt();
    }

    public static TemplateSummaryResponse from(Template template) {
        return new TemplateSummaryResponse(template);
    }
}
