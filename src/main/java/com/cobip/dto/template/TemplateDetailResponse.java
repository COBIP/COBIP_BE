package com.cobip.dto.template;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateVisibility;

import lombok.Getter;

@Getter
public class TemplateDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final TemplateDifficulty difficulty;
    private final TemplateAccessLevel accessLevel;
    private final TemplateVisibility visibility;
    private final List<String> techStacks;
    private final String designIntent;
    private final String requirementsSpec;
    private final String erd;
    private final String apiSpec;
    private final String projectStructure;
    private final List<TemplateInterviewQuestionResponse> interviewQuestions;
    private final String fileUrl;
    private final String thumbnailUrl;
    private final long viewCount;
    private final long favoriteCount;
    private final boolean favorited;
    private final Long ownerId;
    private final String ownerNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private TemplateDetailResponse(Template template, boolean favorited) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.description = template.getDescription();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.accessLevel = template.getAccessLevel();
        this.visibility = template.getVisibility();
        this.techStacks = List.copyOf(template.getTechStacks());
        this.designIntent = template.getDesignIntent();
        this.requirementsSpec = template.getRequirementsSpec();
        this.erd = template.getErd();
        this.apiSpec = template.getApiSpec();
        this.projectStructure = template.getProjectStructure();
        this.interviewQuestions = template.getInterviewQuestions().stream()
                .map(TemplateInterviewQuestionResponse::from)
                .toList();
        this.fileUrl = template.getFileUrl();
        this.thumbnailUrl = template.getThumbnailUrl();
        this.viewCount = template.getViewCount();
        this.favoriteCount = template.getFavoriteCount();
        this.favorited = favorited;
        this.ownerId = template.getOwner().getId();
        this.ownerNickname = template.getOwner().getNickname();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }

    public static TemplateDetailResponse of(Template template, boolean favorited) {
        return new TemplateDetailResponse(template, favorited);
    }
}
