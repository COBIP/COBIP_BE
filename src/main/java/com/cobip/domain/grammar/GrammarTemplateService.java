package com.cobip.domain.grammar;

import java.util.List;
import java.util.Locale;

import com.cobip.dto.grammar.GrammarTemplateCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateChapterCreateRequest;
import com.cobip.dto.grammar.GrammarTemplateChapterResponse;
import com.cobip.dto.grammar.GrammarTemplateChapterUpdateRequest;
import com.cobip.dto.grammar.GrammarTemplateDetailResponse;
import com.cobip.dto.grammar.GrammarTemplateMediaUploadResponse;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileRequest;
import com.cobip.dto.grammar.GrammarTemplatePracticeFileResponse;
import com.cobip.dto.grammar.GrammarTemplatePublicDetailResponse;
import com.cobip.dto.grammar.GrammarTemplatePublicSummaryResponse;
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
    private final GrammarTemplateChapterRepository grammarTemplateChapterRepository;
    private final GrammarTemplatePracticeFileRepository grammarTemplatePracticeFileRepository;
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
        return GrammarTemplateDetailResponse.from(template, List.of());
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
    public List<String> getPublishedCategories(GrammarTemplateLanguage language) {
        return grammarTemplateRepository.findDistinctCategories(GrammarTemplateStatus.PUBLISHED, language);
    }

    @Transactional(readOnly = true)
    public PageResponse<GrammarTemplatePublicSummaryResponse> getPublishedGrammarTemplates(
        String keyword,
        GrammarTemplateLanguage language,
        String category,
        GrammarTemplateDifficulty difficulty,
        Pageable pageable
    ) {
        Page<GrammarTemplatePublicSummaryResponse> templates = grammarTemplateRepository
                .findAll(
                        grammarTemplateSpec(keyword, language, category, difficulty, GrammarTemplateStatus.PUBLISHED),
                        pageable
                )
                .map(GrammarTemplatePublicSummaryResponse::from);
        return PageResponse.from(templates);
    }

    @Transactional(readOnly = true)
    public GrammarTemplatePublicDetailResponse getPublishedGrammarTemplate(Long templateId) {
        GrammarTemplate template = grammarTemplateRepository
                .findByIdAndStatusAndDeletedAtIsNull(templateId, GrammarTemplateStatus.PUBLISHED)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND));
        return GrammarTemplatePublicDetailResponse.from(
                template,
                getActiveChapters(template.getId()),
                getActivePracticeFiles(template.getId())
        );
    }

    @Transactional(readOnly = true)
    public GrammarTemplateDetailResponse getGrammarTemplate(Long templateId) {
        GrammarTemplate template = getActiveTemplate(templateId);
        return GrammarTemplateDetailResponse.from(
                template,
                getActiveChapters(template.getId()),
                getActivePracticeFiles(template.getId())
        );
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
        return GrammarTemplateDetailResponse.from(
                template,
                getActiveChapters(template.getId()),
                getActivePracticeFiles(template.getId())
        );
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
        return GrammarTemplateDetailResponse.from(
                template,
                getActiveChapters(template.getId()),
                getActivePracticeFiles(template.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<GrammarTemplateChapterResponse> getChapters(Long templateId) {
        GrammarTemplate template = getActiveTemplate(templateId);
        return getActiveChapters(template.getId()).stream()
                .map(GrammarTemplateChapterResponse::from)
                .toList();
    }

    @Transactional
    public GrammarTemplateChapterResponse createChapter(
        Long templateId,
        GrammarTemplateChapterCreateRequest request
    ) {
        validateContentJson(request.getContentJson());

        GrammarTemplate template = getActiveTemplate(templateId);
        String searchableText = textExtractor.extract(request.getContentJson());
        GrammarTemplateChapter chapter = grammarTemplateChapterRepository.save(GrammarTemplateChapter.create(
                template,
                request.getTitle(),
                request.getOrderIndex(),
                request.getContentJson(),
                searchableText
        ));
        return GrammarTemplateChapterResponse.from(chapter);
    }

    @Transactional
    public GrammarTemplateChapterResponse updateChapter(
        Long templateId,
        Long chapterId,
        GrammarTemplateChapterUpdateRequest request
    ) {
        GrammarTemplateChapter chapter = getActiveChapter(templateId, chapterId);

        String searchableText = null;
        if (request.getContentJson() != null) {
            validateContentJson(request.getContentJson());
            searchableText = textExtractor.extract(request.getContentJson());
        }

        chapter.update(
                request.getTitle(),
                request.getOrderIndex(),
                request.getContentJson(),
                searchableText
        );
        return GrammarTemplateChapterResponse.from(chapter);
    }

    @Transactional
    public void deleteChapter(Long templateId, Long chapterId) {
        GrammarTemplateChapter chapter = getActiveChapter(templateId, chapterId);
        chapter.delete();
    }

    @Transactional(readOnly = true)
    public List<GrammarTemplatePracticeFileResponse> getPracticeFiles(Long templateId, Long chapterId) {
        getActiveTemplate(templateId);
        getActiveChapter(templateId, chapterId);
        return getActivePracticeFiles(templateId, chapterId).stream()
                .map(GrammarTemplatePracticeFileResponse::from)
                .toList();
    }

    @Transactional
    public GrammarTemplatePracticeFileResponse createPracticeFile(
        Long templateId,
        Long chapterId,
        GrammarTemplatePracticeFileRequest request
    ) {
        GrammarTemplate template = getActiveTemplate(templateId);
        GrammarTemplateChapter chapter = getActiveChapter(templateId, chapterId);
        GrammarTemplatePracticeFile file = GrammarTemplatePracticeFile.create(
                template,
                chapter,
                request.getNodeType(),
                request.getFilePath(),
                request.getLanguage(),
                request.getContent(),
                request.getReadOnly(),
                request.getOrderIndex()
        );
        return GrammarTemplatePracticeFileResponse.from(grammarTemplatePracticeFileRepository.save(file));
    }

    @Transactional
    public GrammarTemplatePracticeFileResponse updatePracticeFile(
        Long templateId,
        Long chapterId,
        Long fileId,
        GrammarTemplatePracticeFileRequest request
    ) {
        getActiveTemplate(templateId);
        getActiveChapter(templateId, chapterId);
        GrammarTemplatePracticeFile file = getActivePracticeFile(templateId, chapterId, fileId);
        file.update(
                request.getNodeType(),
                request.getFilePath(),
                request.getLanguage(),
                request.getContent(),
                request.getReadOnly(),
                request.getOrderIndex()
        );
        return GrammarTemplatePracticeFileResponse.from(file);
    }

    @Transactional
    public void deletePracticeFile(Long templateId, Long chapterId, Long fileId) {
        getActiveTemplate(templateId);
        getActiveChapter(templateId, chapterId);
        GrammarTemplatePracticeFile file = getActivePracticeFile(templateId, chapterId, fileId);
        grammarTemplatePracticeFileRepository.delete(file);
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

    private List<GrammarTemplateChapter> getActiveChapters(Long templateId) {
        return grammarTemplateChapterRepository.findByTemplateIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(templateId);
    }

    private GrammarTemplateChapter getActiveChapter(Long templateId, Long chapterId) {
        return grammarTemplateChapterRepository.findByIdAndTemplateIdAndDeletedAtIsNull(chapterId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_CHAPTER_NOT_FOUND));
    }

    private List<GrammarTemplatePracticeFile> getActivePracticeFiles(Long templateId) {
        return grammarTemplatePracticeFileRepository.findByTemplateIdOrderByChapterIdAscOrderIndexAscIdAsc(templateId);
    }

    private List<GrammarTemplatePracticeFile> getActivePracticeFiles(Long templateId, Long chapterId) {
        return grammarTemplatePracticeFileRepository.findByTemplateIdAndChapterIdOrderByOrderIndexAscIdAsc(
                templateId,
                chapterId
        );
    }

    private GrammarTemplatePracticeFile getActivePracticeFile(Long templateId, Long chapterId, Long fileId) {
        return grammarTemplatePracticeFileRepository.findByIdAndTemplateIdAndChapterId(fileId, templateId, chapterId)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_PRACTICE_FILE_NOT_FOUND));
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
