package com.cobip.domain.template;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.template.TemplateCreateRequest;
import com.cobip.dto.template.TemplateDetailResponse;
import com.cobip.dto.template.TemplateFileUploadResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.template.TemplateUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.aws.S3Service;
import com.cobip.infra.aws.S3UploadResult;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateFavoriteRepository templateFavoriteRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final ActivityHistoryService activityHistoryService;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryResponse> getTemplates(
        String keyword,
        String category,
        TemplateDifficulty difficulty,
        Pageable pageable
    ) {
        Page<TemplateSummaryResponse> templates = templateRepository
                .findAll(publicTemplateSpec(keyword, category, difficulty), pageable)
                .map(TemplateSummaryResponse::from);
        return PageResponse.from(templates);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return templateRepository.findPublicCategories();
    }

    @Transactional(readOnly = true)
    public List<String> getTechStacks() {
        return templateRepository.findPublicTechStacks();
    }

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryResponse> getRecommendedTemplates(User user, Pageable pageable) {
        Set<String> categories = recommendationCategories(user);
        Page<Template> templates = categories.isEmpty()
                ? templateRepository.findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
                        TemplateVisibility.PUBLIC,
                        pageable
                )
                : templateRepository.findAll(recommendedTemplateSpec(categories), pageable);
        return PageResponse.from(templates.map(TemplateSummaryResponse::from));
    }

    @Transactional
    public TemplateDetailResponse getTemplate(Long templateId, User currentUser) {
        Template template = getActiveTemplate(templateId);
        validateReadable(template, currentUser);
        template.increaseViewCount();
        boolean favorited = currentUser != null
                && templateFavoriteRepository.existsByUserIdAndTemplateId(currentUser.getId(), template.getId());
        return TemplateDetailResponse.of(template, favorited);
    }

    @Transactional
    public TemplateDetailResponse createTemplate(User owner, TemplateCreateRequest request) {
        User managedOwner = getManagedUser(owner);
        Template template = templateRepository.save(Template.create(managedOwner, request));
        activityHistoryService.record(managedOwner, ActivityType.TEMPLATE_CREATED, "템플릿을 등록했습니다.", "TEMPLATE", template.getId());
        return TemplateDetailResponse.of(template, false);
    }

    @Transactional
    public TemplateDetailResponse updateTemplate(User user, Long templateId, TemplateUpdateRequest request) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        validateOwner(template, managedUser);
        template.update(request);
        activityHistoryService.record(managedUser, ActivityType.TEMPLATE_UPDATED, "템플릿을 수정했습니다.", "TEMPLATE", template.getId());
        boolean favorited = templateFavoriteRepository.existsByUserIdAndTemplateId(managedUser.getId(), template.getId());
        return TemplateDetailResponse.of(template, favorited);
    }

    @Transactional
    public void deleteTemplate(User user, Long templateId) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        validateOwner(template, managedUser);
        template.delete();
        activityHistoryService.record(managedUser, ActivityType.TEMPLATE_DELETED, "템플릿을 삭제했습니다.", "TEMPLATE", template.getId());
    }

    @Transactional
    public TemplateFileUploadResponse uploadFile(User user, Long templateId, MultipartFile file) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        validateOwner(template, managedUser);
        S3UploadResult result = s3Service.uploadTemplateFile(templateId, file);
        template.uploadFile(result.key(), result.url());
        activityHistoryService.record(managedUser, ActivityType.TEMPLATE_FILE_UPLOADED, "템플릿 파일을 업로드했습니다.", "TEMPLATE", template.getId());
        return new TemplateFileUploadResponse(template.getId(), result.key(), result.url());
    }

    @Transactional
    public TemplateFileUploadResponse uploadThumbnail(User user, Long templateId, MultipartFile file) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        validateOwner(template, managedUser);
        S3UploadResult result = s3Service.uploadThumbnail(templateId, file);
        template.uploadThumbnail(result.key(), result.url());
        activityHistoryService.record(managedUser, ActivityType.TEMPLATE_THUMBNAIL_UPLOADED, "템플릿 썸네일을 업로드했습니다.", "TEMPLATE", template.getId());
        return new TemplateFileUploadResponse(template.getId(), result.key(), result.url());
    }

    @Transactional
    public void addFavorite(User user, Long templateId) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        validatePublicTemplate(template);
        if (templateFavoriteRepository.existsByUserIdAndTemplateId(managedUser.getId(), template.getId())) {
            throw new CustomException(ErrorCode.DUPLICATE_FAVORITE);
        }
        templateFavoriteRepository.save(TemplateFavorite.builder().user(managedUser).template(template).build());
        template.increaseFavoriteCount();
        activityHistoryService.record(managedUser, ActivityType.TEMPLATE_FAVORITED, "템플릿을 찜했습니다.", "TEMPLATE", template.getId());
    }

    @Transactional
    public void removeFavorite(User user, Long templateId) {
        User managedUser = getManagedUser(user);
        Template template = getActiveTemplate(templateId);
        templateFavoriteRepository.findByUserIdAndTemplateId(managedUser.getId(), template.getId())
                .ifPresent(templateFavorite -> {
                    templateFavoriteRepository.delete(templateFavorite);
                    template.decreaseFavoriteCount();
                    activityHistoryService.record(
                            managedUser,
                            ActivityType.TEMPLATE_UNFAVORITED,
                            "템플릿 찜을 취소했습니다.",
                            "TEMPLATE",
                            template.getId()
                    );
                });
    }

    public Template getActiveTemplate(Long templateId) {
        return templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    private void validateReadable(Template template, User currentUser) {
        if (template.isOwner(currentUser)) {
            return;
        }
        validatePublicTemplate(template);
        if (template.isPremium() && !subscriptionService.hasActiveSubscription(currentUser)) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_REQUIRED);
        }
    }

    private void validateOwner(Template template, User user) {
        if (!template.isOwner(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validatePublicTemplate(Template template) {
        if (!template.isPublic() || template.isDeleted()) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
    }

    private Specification<Template> publicTemplateSpec(String keyword, String category, TemplateDifficulty difficulty) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            var predicate = criteriaBuilder.and(
                    criteriaBuilder.isNull(root.get("deletedAt")),
                    criteriaBuilder.equal(root.get("visibility"), TemplateVisibility.PUBLIC)
            );

            if (category != null && !category.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                Join<Template, String> techStackJoin = root.join("techStacks", JoinType.LEFT);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(techStackJoin), likeKeyword)
                ));
            }
            return predicate;
        };
    }

    private Set<String> recommendationCategories(User user) {
        Set<String> categories = new LinkedHashSet<>();
        if (user == null) {
            return categories;
        }

        learningProgressRepository.findTop5ByUserIdOrderByLastAccessedAtDesc(user.getId())
                .stream()
                .map(progress -> progress.getTemplate().getCategory())
                .filter(category -> category != null && !category.isBlank())
                .forEach(categories::add);

        templateFavoriteRepository.findByUserId(user.getId(), PageRequest.of(0, 20))
                .stream()
                .map(favorite -> favorite.getTemplate().getCategory())
                .filter(category -> category != null && !category.isBlank())
                .forEach(categories::add);

        return categories;
    }

    private Specification<Template> recommendedTemplateSpec(Set<String> categories) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.and(
                    criteriaBuilder.isNull(root.get("deletedAt")),
                    criteriaBuilder.equal(root.get("visibility"), TemplateVisibility.PUBLIC),
                    root.get("category").in(categories)
            );
        };
    }
}
