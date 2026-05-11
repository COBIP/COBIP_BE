package com.cobip.dto.grammar;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplatePracticeFile;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class GrammarTemplateChapterResponse {

    private final Long id;
    private final Long templateId;
    private final String title;
    private final Integer orderIndex;
    private final JsonNode contentJson;
    private final List<GrammarTemplatePracticeFileResponse> practiceFiles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private GrammarTemplateChapterResponse(
        GrammarTemplateChapter chapter,
        List<GrammarTemplatePracticeFile> practiceFiles
    ) {
        this.id = chapter.getId();
        this.templateId = chapter.getTemplate().getId();
        this.title = chapter.getTitle();
        this.orderIndex = chapter.getOrderIndex();
        this.contentJson = chapter.getContentJson();
        this.practiceFiles = practiceFiles.stream()
                .map(GrammarTemplatePracticeFileResponse::from)
                .toList();
        this.createdAt = chapter.getCreatedAt();
        this.updatedAt = chapter.getUpdatedAt();
    }

    public static GrammarTemplateChapterResponse from(GrammarTemplateChapter chapter) {
        return from(chapter, List.of());
    }

    public static GrammarTemplateChapterResponse from(
        GrammarTemplateChapter chapter,
        List<GrammarTemplatePracticeFile> practiceFiles
    ) {
        return new GrammarTemplateChapterResponse(chapter, practiceFiles);
    }
}
