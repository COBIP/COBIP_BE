package com.cobip.domain.grammar;

import java.util.Locale;

import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateDetailResponse;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplateSummaryResponse;
import com.cobip.dto.grammar.GrammarTemplateUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.aws.S3Service;
import com.cobip.infra.aws.S3UploadResult;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrammarTemplateService {

    private final GrammarTemplateRepository grammarTemplateRepository;
    private final GrammarTemplateTextExtractor textExtractor;
    private final S3Service s3Service;

    @Transactional
    public GrammarTemplateDetailResponse createGrammarTemplate(GrammarTemplateCreateRequest request) {
        validateContentJson(request.getContentJson());
        validateUniqueSlug(request.getSlug());

        String searchableText = textExtractor.extract(request.getContentJson());
        GrammarTemplate template = grammarTemplateRepository.save(GrammarTemplate.create(
                request.getSlug(),
                request.getTitle(),
                request.getLanguage(),
                request.getCategory(),
                request.getDifficulty(),
                request.getSummary(),
                request.getContentJson(),
                request.getStatus(),
                searchableText
        ));
        return GrammarTemplateDetailResponse.from(template);
    }

    @Transactional(readOnly = true)
    public PageResponse<GrammarTemplateSummaryResponse> getGrammarTemplates(
        String keyword,
        GrammarTemplateLanguage language,
        String category,
        GrammarTemplateDifficulty difficulty,
        GrammarTemplateStatus status,
        Pageable pageable
    ) {
        Page<GrammarTemplateSummaryResponse> templates = grammarTemplateRepository
                .findAll(grammarTemplateSpec(keyword, language, category, difficulty, status), pageable)
                .map(GrammarTemplateSummaryResponse::from);
        return PageResponse.from(templates);
    }

    @Transactional(readOnly = true)
    public GrammarTemplateDetailResponse getGrammarTemplate(Long templateId) {
        return GrammarTemplateDetailResponse.from(getActiveTemplate(templateId));
    }

    @Transactional
    public GrammarTemplateDetailResponse updateGrammarTemplate(Long templateId, GrammarTemplateUpdateRequest request) {
        GrammarTemplate template = getActiveTemplate(templateId);
        if (request.getSlug() != null) {
            validateUniqueSlugForUpdate(request.getSlug(), template.getId());
        }

        String searchableText = null;
        if (request.getContentJson() != null) {
            validateContentJson(request.getContentJson());
            searchableText = textExtractor.extract(request.getContentJson());
        }

        template.update(
                request.getSlug(),
                request.getTitle(),
                request.getLanguage(),
                request.getCategory(),
                request.getDifficulty(),
                request.getSummary(),
                request.getContentJson(),
                searchableText
        );
        return GrammarTemplateDetailResponse.from(template);
    }

    @Transactional
    public void deleteGrammarTemplate(Long templateId) {
        GrammarTemplate template = getActiveTemplate(templateId);
        template.delete();
    }

    @Transactional
    public GrammarTemplateDetailResponse changeStatus(Long templateId, GrammarTemplateStatus status) {
        GrammarTemplate template = getActiveTemplate(templateId);
        template.changeStatus(status);
        return GrammarTemplateDetailResponse.from(template);
    }

    @Transactional(readOnly = true)
    public GrammarTemplateMediaUploadResponse uploadMedia(
        Long templateId,
        GrammarTemplateMediaType mediaType,
        MultipartFile file
    ) {
        GrammarTemplate template = getActiveTemplate(templateId);
        S3UploadResult result = s3Service.uploadGrammarTemplateMedia(template.getId(), mediaType, file);
        return new GrammarTemplateMediaUploadResponse(
                template.getId(),
                mediaType,
                result.key(),
                result.url(),
                file.getContentType()
        );
    }

    private GrammarTemplate getActiveTemplate(Long templateId) {
        return grammarTemplateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND));
    }

    private void validateUniqueSlug(String slug) {
        if (grammarTemplateRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new CustomException(ErrorCode.DUPLICATE_GRAMMAR_TEMPLATE_SLUG);
        }
    }

    private void validateUniqueSlugForUpdate(String slug, Long templateId) {
        if (grammarTemplateRepository.existsBySlugAndDeletedAtIsNullAndIdNot(slug, templateId)) {
            throw new CustomException(ErrorCode.DUPLICATE_GRAMMAR_TEMPLATE_SLUG);
        }
    }

    private void validateContentJson(JsonNode contentJson) {
        if (contentJson == null || contentJson.isNull() || !contentJson.isContainerNode()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Specification<GrammarTemplate> grammarTemplateSpec(
        String keyword,
        GrammarTemplateLanguage language,
        String category,
        GrammarTemplateDifficulty difficulty,
        GrammarTemplateStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.isNull(root.get("deletedAt"));

            if (language != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("language"), language));
            }
            if (category != null && !category.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("searchableText")), likeKeyword)
                ));
            }
            return predicate;
        };
    }
}
