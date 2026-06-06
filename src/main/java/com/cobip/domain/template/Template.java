package com.cobip.domain.template;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminTemplateCreateRequest;
import com.cobip.dto.admin.AdminTemplateInterviewQuestionRequest;
import com.cobip.dto.admin.AdminTemplateNextRecommendationRequest;
import com.cobip.dto.admin.AdminTemplateUpdateRequest;
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
import jakarta.persistence.OrderBy;
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

    @Column(length = 500)
    private String summary;

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

    @ElementCollection
    @CollectionTable(name = "template_tags", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "tag", nullable = false, length = 80)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(length = 40)
    private String runtime;

    @Column(name = "license", length = 80)
    private String license;

    @Column(length = 120)
    private String source;

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
    @Builder.Default
    private List<TemplateInterviewQuestion> interviewQuestions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "template_next_recommendations", joinColumns = @JoinColumn(name = "template_id"))
    @OrderBy("priority ASC")
    @Builder.Default
    private List<TemplateNextRecommendation> nextRecommendations = new ArrayList<>();

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
                .techStacks(new ArrayList<>(listOrEmpty(request.getTechStacks())))
                .tags(new ArrayList<>())
                .designIntent(request.getDesignIntent())
                .requirementsSpec(request.getRequirementsSpec())
                .erd(request.getErd())
                .apiSpec(request.getApiSpec())
                .projectStructure(request.getProjectStructure())
                .interviewQuestions(toInterviewQuestions(request.getInterviewQuestions()))
                .visibility(request.getVisibility())
                .accessLevel(request.getAccessLevel())
                .viewCount(0)
                .favoriteCount(0)
                .build();
    }

    public static Template create(User owner, AdminTemplateCreateRequest request) {
        TemplateVisibility visibility = request.getPublished() != null
                ? visibilityFromPublished(request.getPublished())
                : request.getVisibility();
        return Template.builder()
                .owner(owner)
                .title(request.getTitle())
                .summary(request.getSummary())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .techStacks(new ArrayList<>(listOrEmpty(request.getTechStacks())))
                .tags(new ArrayList<>(listOrEmpty(request.getTags())))
                .runtime(request.getRuntime())
                .license(request.getLicense())
                .source(request.getSource())
                .designIntent(request.getDesignIntent())
                .requirementsSpec(request.getRequirementsSpec())
                .erd(request.getErd())
                .apiSpec(request.getApiSpec())
                .projectStructure(request.getProjectStructure())
                .interviewQuestions(toAdminInterviewQuestions(request.getInterviewQuestions()))
                .nextRecommendations(toNextRecommendations(request.getNextRecommendations()))
                .visibility(visibility == null ? TemplateVisibility.PUBLIC : visibility)
                .accessLevel(request.getAccessLevel() == null ? TemplateAccessLevel.FREE : request.getAccessLevel())
                .thumbnailUrl(request.getPreviewImage())
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
            this.interviewQuestions = toInterviewQuestions(request.getInterviewQuestions());
        }
        if (request.getVisibility() != null) {
            this.visibility = request.getVisibility();
        }
        if (request.getAccessLevel() != null) {
            this.accessLevel = request.getAccessLevel();
        }
    }

    public void update(AdminTemplateUpdateRequest request) {
        if (request.getTitle() != null) {
            this.title = request.getTitle();
        }
        if (request.getSummary() != null) {
            this.summary = request.getSummary();
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
        if (request.getTags() != null) {
            this.tags = new ArrayList<>(request.getTags());
        }
        if (request.getRuntime() != null) {
            this.runtime = request.getRuntime();
        }
        if (request.getLicense() != null) {
            this.license = request.getLicense();
        }
        if (request.getSource() != null) {
            this.source = request.getSource();
        }
        if (request.getPreviewImage() != null) {
            this.thumbnailUrl = request.getPreviewImage();
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
            this.interviewQuestions = toAdminInterviewQuestions(request.getInterviewQuestions());
        }
        if (request.getNextRecommendations() != null) {
            this.nextRecommendations = toNextRecommendations(request.getNextRecommendations());
        }
        if (request.getPublished() != null) {
            this.visibility = visibilityFromPublished(request.getPublished());
        } else if (request.getVisibility() != null) {
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

    private static List<TemplateInterviewQuestion> toInterviewQuestions(List<String> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        return questions.stream()
                .map(question -> TemplateInterviewQuestion.of(question, ""))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static List<TemplateInterviewQuestion> toAdminInterviewQuestions(
        List<AdminTemplateInterviewQuestionRequest> questions
    ) {
        if (questions == null) {
            return new ArrayList<>();
        }
        return questions.stream()
                .map(question -> TemplateInterviewQuestion.of(question.getQuestion(), question.getAnswerHint()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static List<TemplateNextRecommendation> toNextRecommendations(
        List<AdminTemplateNextRecommendationRequest> recommendations
    ) {
        if (recommendations == null) {
            return new ArrayList<>();
        }
        return recommendations.stream()
                .map(recommendation -> TemplateNextRecommendation.of(
                        recommendation.getFeatureName(),
                        recommendation.getReason(),
                        recommendation.getExpectedLearning(),
                        recommendation.getPriority()
                ))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static TemplateVisibility visibilityFromPublished(boolean published) {
        return published ? TemplateVisibility.PUBLIC : TemplateVisibility.PRIVATE;
    }

    private static <T> List<T> listOrEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }
}
