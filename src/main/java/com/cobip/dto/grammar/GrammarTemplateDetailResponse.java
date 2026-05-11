package com.cobip.dto.grammar;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class GrammarTemplateDetailResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final GrammarTemplateLanguage language;
    private final String category;
    private final GrammarTemplateDifficulty difficulty;
    private final String summary;
    private final JsonNode contentJson;
    private final List<GrammarTemplateChapterResponse> chapters;
    private final String searchableText;
    private final GrammarTemplateStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private GrammarTemplateDetailResponse(GrammarTemplate template, List<GrammarTemplateChapter> chapters) {
        this.id = template.getId();
        this.slug = template.getSlug();
        this.title = template.getTitle();
        this.language = template.getLanguage();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.summary = template.getSummary();
        this.contentJson = template.getContentJson();
        this.chapters = chapters.stream()
                .map(GrammarTemplateChapterResponse::from)
                .toList();
        this.searchableText = template.getSearchableText();
        this.status = template.getStatus();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
        this.deletedAt = template.getDeletedAt();
    }

    public static GrammarTemplateDetailResponse from(GrammarTemplate template) {
        return from(template, List.of());
    }

    public static GrammarTemplateDetailResponse from(
        GrammarTemplate template,
        List<GrammarTemplateChapter> chapters
    ) {
        return new GrammarTemplateDetailResponse(template, chapters);
    }
}
