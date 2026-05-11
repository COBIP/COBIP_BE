package com.cobip.dto.grammar;

import java.time.LocalDateTime;

import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class GrammarTemplateChapterResponse {

    private final Long id;
    private final Long templateId;
    private final String title;
    private final Integer orderIndex;
    private final JsonNode contentJson;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private GrammarTemplateChapterResponse(GrammarTemplateChapter chapter) {
        this.id = chapter.getId();
        this.templateId = chapter.getTemplate().getId();
        this.title = chapter.getTitle();
        this.orderIndex = chapter.getOrderIndex();
        this.contentJson = chapter.getContentJson();
        this.createdAt = chapter.getCreatedAt();
        this.updatedAt = chapter.getUpdatedAt();
    }

    public static GrammarTemplateChapterResponse from(GrammarTemplateChapter chapter) {
        return new GrammarTemplateChapterResponse(chapter);
    }
}
