package com.cobip.domain.template;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.user.User;
import com.cobip.dto.template.TemplateCreateRequest;
import com.cobip.dto.template.TemplateUpdateRequest;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "templates")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Template extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 120)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TemplateDifficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "template_tech_stacks", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "tech_stack", nullable = false, length = 80)
    @Builder.Default
    private List<String> techStacks = new ArrayList<>();

    @Lob
    private String designIntent;

    @Lob
    private String requirementsSpec;

    @Lob
    private String erd;

    @Lob
    private String apiSpec;

    @Lob
    private String projectStructure;

    @ElementCollection
    @CollectionTable(name = "template_interview_questions", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "question", nullable = false, length = 1000)
    @Builder.Default
    private List<String> interviewQuestions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateAccessLevel accessLevel;

    @Column(length = 1000)
    private String fileUrl;

    @Column(length = 500)
    private String fileKey;

    @Column(length = 1000)
    private String thumbnailUrl;

    @Column(length = 500)
    private String thumbnailKey;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private long favoriteCount;

    private LocalDateTime deletedAt;

    public static Template create(User owner, TemplateCreateRequest request) {
        return Template.builder()
                .owner(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .techStacks(new ArrayList<>(request.getTechStacks()))
                .designIntent(request.getDesignIntent())
                .requirementsSpec(request.getRequirementsSpec())
                .erd(request.getErd())
                .apiSpec(request.getApiSpec())
                .projectStructure(request.getProjectStructure())
                .interviewQuestions(new ArrayList<>(request.getInterviewQuestions()))
                .visibility(request.getVisibility())
                .accessLevel(request.getAccessLevel())
                .viewCount(0)
                .favoriteCount(0)
                .build();
    }

    public void update(TemplateUpdateRequest request) {
        if (request.getTitle() != null) {
            this.title = request.getTitle();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription();
        }
        if (request.getCategory() != null) {
            this.category = request.getCategory();
        }
        if (request.getDifficulty() != null) {
            this.difficulty = request.getDifficulty();
        }
        if (request.getTechStacks() != null) {
            this.techStacks = new ArrayList<>(request.getTechStacks());
        }
        if (request.getDesignIntent() != null) {
            this.designIntent = request.getDesignIntent();
        }
        if (request.getRequirementsSpec() != null) {
            this.requirementsSpec = request.getRequirementsSpec();
        }
        if (request.getErd() != null) {
            this.erd = request.getErd();
        }
        if (request.getApiSpec() != null) {
            this.apiSpec = request.getApiSpec();
        }
        if (request.getProjectStructure() != null) {
            this.projectStructure = request.getProjectStructure();
        }
        if (request.getInterviewQuestions() != null) {
            this.interviewQuestions = new ArrayList<>(request.getInterviewQuestions());
        }
        if (request.getVisibility() != null) {
            this.visibility = request.getVisibility();
        }
        if (request.getAccessLevel() != null) {
            this.accessLevel = request.getAccessLevel();
        }
    }

    public boolean isOwner(User user) {
        return user != null && owner.getId().equals(user.getId());
    }

    public boolean isPublic() {
        return visibility == TemplateVisibility.PUBLIC;
    }

    public boolean isPremium() {
        return accessLevel == TemplateAccessLevel.PREMIUM;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    public void decreaseFavoriteCount() {
        if (favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    public void uploadFile(String fileKey, String fileUrl) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    public void uploadThumbnail(String thumbnailKey, String thumbnailUrl) {
        this.thumbnailKey = thumbnailKey;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void changeExposure(TemplateVisibility visibility, TemplateAccessLevel accessLevel) {
        this.visibility = visibility;
        this.accessLevel = accessLevel;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
