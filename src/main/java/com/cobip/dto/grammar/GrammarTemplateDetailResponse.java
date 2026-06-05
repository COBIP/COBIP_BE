package com.cobip.dto.grammar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplateChapterMission;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplatePracticeFile;
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

    private GrammarTemplateDetailResponse(
        GrammarTemplate template,
        List<GrammarTemplateChapter> chapters,
        List<GrammarTemplatePracticeFile> practiceFiles,
        List<GrammarTemplateChapterMission> missions
    ) {
        Map<Long, List<GrammarTemplatePracticeFile>> practiceFilesByChapterId = practiceFiles.stream()
                .collect(Collectors.groupingBy(file -> file.getChapter().getId()));
        Map<Long, List<GrammarTemplateChapterMission>> missionsByChapterId = missions.stream()
                .collect(Collectors.groupingBy(mission -> mission.getChapter().getId()));

        this.id = template.getId();
        this.slug = template.getSlug();
        this.title = template.getTitle();
        this.language = template.getLanguage();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.summary = template.getSummary();
        this.contentJson = template.getContentJson();
        this.chapters = chapters.stream()
                .map(chapter -> GrammarTemplateChapterResponse.from(
                        chapter,
                        practiceFilesByChapterId.getOrDefault(chapter.getId(), List.of()),
                        missionsByChapterId.getOrDefault(chapter.getId(), List.of())
                ))
                .toList();
        this.searchableText = template.getSearchableText();
        this.status = template.getStatus();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
        this.deletedAt = template.getDeletedAt();
    }

    public static GrammarTemplateDetailResponse from(GrammarTemplate template) {
        return from(template, List.of(), List.of(), List.of());
    }

    public static GrammarTemplateDetailResponse from(
        GrammarTemplate template,
        List<GrammarTemplateChapter> chapters
    ) {
        return from(template, chapters, List.of(), List.of());
    }

    public static GrammarTemplateDetailResponse from(
        GrammarTemplate template,
        List<GrammarTemplateChapter> chapters,
        List<GrammarTemplatePracticeFile> practiceFiles
    ) {
        return from(template, chapters, practiceFiles, List.of());
    }

    public static GrammarTemplateDetailResponse from(
        GrammarTemplate template,
        List<GrammarTemplateChapter> chapters,
        List<GrammarTemplatePracticeFile> practiceFiles,
        List<GrammarTemplateChapterMission> missions
    ) {
        return new GrammarTemplateDetailResponse(template, chapters, practiceFiles, missions);
    }
}
