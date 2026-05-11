package com.cobip.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.practice.TemplatePracticeFile;
import com.cobip.domain.practice.TemplatePracticeMission;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateTestCase;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.dto.practice.TemplatePracticeMissionResponse;

import lombok.Getter;

@Getter
public class AdminTemplateDetailResponse {

    private final Long id;
    private final String title;
    private final String summary;
    private final String description;
    private final String category;
    private final TemplateDifficulty difficulty;
    private final TemplateVisibility visibility;
    private final TemplateAccessLevel accessLevel;
    private final boolean published;
    private final List<String> techStacks;
    private final List<String> tags;
    private final String runtime;
    private final String previewImage;
    private final String license;
    private final String source;
    private final String designIntent;
    private final String requirementsSpec;
    private final String erd;
    private final String apiSpec;
    private final String projectStructure;
    private final List<AdminTemplateInterviewQuestionResponse> interviewQuestions;
    private final List<AdminTemplatePracticeFileResponse> practiceFiles;
    private final List<TemplatePracticeMissionResponse> missions;
    private final List<AdminTemplateTestCaseResponse> testCases;
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

    private AdminTemplateDetailResponse(
        Template template,
        List<TemplatePracticeFile> practiceFiles,
        List<TemplatePracticeMission> missions,
        List<TemplateTestCase> testCases
    ) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.summary = template.getSummary();
        this.description = template.getDescription();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.visibility = template.getVisibility();
        this.accessLevel = template.getAccessLevel();
        this.published = template.getVisibility() == TemplateVisibility.PUBLIC;
        this.techStacks = List.copyOf(template.getTechStacks());
        this.tags = List.copyOf(template.getTags());
        this.runtime = template.getRuntime();
        this.previewImage = template.getThumbnailUrl();
        this.license = template.getLicense();
        this.source = template.getSource();
        this.designIntent = template.getDesignIntent();
        this.requirementsSpec = template.getRequirementsSpec();
        this.erd = template.getErd();
        this.apiSpec = template.getApiSpec();
        this.projectStructure = template.getProjectStructure();
        this.interviewQuestions = template.getInterviewQuestions().stream()
                .map(AdminTemplateInterviewQuestionResponse::from)
                .toList();
        this.practiceFiles = practiceFiles.stream()
                .map(AdminTemplatePracticeFileResponse::from)
                .toList();
        this.missions = missions.stream()
                .map(TemplatePracticeMissionResponse::from)
                .toList();
        this.testCases = testCases.stream()
                .map(AdminTemplateTestCaseResponse::from)
                .toList();
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
        return of(template, List.of(), List.of(), List.of());
    }

    public static AdminTemplateDetailResponse of(
        Template template,
        List<TemplatePracticeFile> practiceFiles,
        List<TemplatePracticeMission> missions,
        List<TemplateTestCase> testCases
    ) {
        return new AdminTemplateDetailResponse(template, practiceFiles, missions, testCases);
    }
}
