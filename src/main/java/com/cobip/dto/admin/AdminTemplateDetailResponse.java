package com.cobip.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import lombok.Getter;

@Getter
public class AdminTemplateDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final TemplateDifficulty difficulty;
    private final TemplateVisibility visibility;
    private final TemplateAccessLevel accessLevel;
    private final List<String> techStacks;
    private final String designIntent;
    private final String requirementsSpec;
    private final String erd;
    private final String apiSpec;
    private final String projectStructure;
    private final List<String> interviewQuestions;
    private final String fileUrl;
    private final String fileKey;
    private final String thumbnailUrl;
    private final String thumbnailKey;
    private final long viewCount;
    private final long favoriteCount;
    private final Long ownerId;
    private final String ownerNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private AdminTemplateDetailResponse(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.description = template.getDescription();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.visibility = template.getVisibility();
        this.accessLevel = template.getAccessLevel();
        this.techStacks = List.copyOf(template.getTechStacks());
        this.designIntent = template.getDesignIntent();
        this.requirementsSpec = template.getRequirementsSpec();
        this.erd = template.getErd();
        this.apiSpec = template.getApiSpec();
        this.projectStructure = template.getProjectStructure();
        this.interviewQuestions = List.copyOf(template.getInterviewQuestions());
        this.fileUrl = template.getFileUrl();
        this.fileKey = template.getFileKey();
        this.thumbnailUrl = template.getThumbnailUrl();
        this.thumbnailKey = template.getThumbnailKey();
        this.viewCount = template.getViewCount();
        this.favoriteCount = template.getFavoriteCount();
        this.ownerId = template.getOwner().getId();
        this.ownerNickname = template.getOwner().getNickname();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }

    public static AdminTemplateDetailResponse from(Template template) {
        return new AdminTemplateDetailResponse(template);
    }
}
