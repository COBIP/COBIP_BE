package com.cobip.domain.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminTemplateDetailResponse;
import com.cobip.dto.admin.AdminTemplateExposureUpdateRequest;
import com.cobip.dto.admin.AdminTemplateSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTemplateService {

    private final TemplateRepository templateRepository;
    private final ActivityHistoryService activityHistoryService;

    @Transactional(readOnly = true)
    public PageResponse<AdminTemplateSummaryResponse> getTemplates(
        String keyword,
        String category,
        TemplateDifficulty difficulty,
        TemplateVisibility visibility,
        TemplateAccessLevel accessLevel,
        Long ownerId,
        Pageable pageable
    ) {
        return PageResponse.from(templateRepository.findAll(
                templateSpec(keyword, category, difficulty, visibility, accessLevel, ownerId),
                pageable
        ).map(AdminTemplateSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminTemplateDetailResponse getTemplate(Long templateId) {
        return AdminTemplateDetailResponse.from(findActiveTemplate(templateId));
    }

    @Transactional
    public AdminTemplateDetailResponse updateExposure(
        Long templateId,
        AdminTemplateExposureUpdateRequest request,
        User adminUser
    ) {
        Template template = findActiveTemplate(templateId);
        template.changeExposure(request.getVisibility(), request.getAccessLevel());

        if (adminUser != null) {
            activityHistoryService.record(
                    adminUser,
                    ActivityType.TEMPLATE_UPDATED,
                    "Admin updated template exposure.",
                    "TEMPLATE",
                    template.getId()
            );
        }

        return AdminTemplateDetailResponse.from(template);
    }

    private Template findActiveTemplate(Long templateId) {
        return templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    private Specification<Template> templateSpec(
        String keyword,
        String category,
        TemplateDifficulty difficulty,
        TemplateVisibility visibility,
        TemplateAccessLevel accessLevel,
        Long ownerId
    ) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (visibility != null) {
                predicates.add(criteriaBuilder.equal(root.get("visibility"), visibility));
            }
            if (accessLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("accessLevel"), accessLevel));
            }
            if (ownerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), ownerId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                Join<Template, User> ownerJoin = root.join("owner", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(ownerJoin.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(ownerJoin.get("nickname")), likeKeyword)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
