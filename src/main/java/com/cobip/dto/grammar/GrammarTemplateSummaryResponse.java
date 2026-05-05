package com.cobip.dto.grammar;

import java.time.LocalDateTime;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateStatus;

import lombok.Getter;

@Getter
public class GrammarTemplateSummaryResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final GrammarTemplateLanguage language;
    private final String category;
    private final GrammarTemplateDifficulty difficulty;
    private final String summary;
    private final GrammarTemplateStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private GrammarTemplateSummaryResponse(GrammarTemplate template) {
        this.id = template.getId();
        this.slug = template.getSlug();
        this.title = template.getTitle();
        this.language = template.getLanguage();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.summary = template.getSummary();
        this.status = template.getStatus();
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }

    public static GrammarTemplateSummaryResponse from(GrammarTemplate template) {
        return new GrammarTemplateSummaryResponse(template);
    }
}
