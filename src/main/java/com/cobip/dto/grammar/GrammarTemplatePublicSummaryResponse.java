package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;

import lombok.Getter;

@Getter
public class GrammarTemplatePublicSummaryResponse {

    private final Long id;
    private final String slug;
    private final String title;
    private final GrammarTemplateLanguage language;
    private final String category;
    private final GrammarTemplateDifficulty difficulty;
    private final String summary;

    private GrammarTemplatePublicSummaryResponse(GrammarTemplate template) {
        this.id = template.getId();
        this.slug = template.getSlug();
        this.title = template.getTitle();
        this.language = template.getLanguage();
        this.category = template.getCategory();
        this.difficulty = template.getDifficulty();
        this.summary = template.getSummary();
    }

    public static GrammarTemplatePublicSummaryResponse from(GrammarTemplate template) {
        return new GrammarTemplatePublicSummaryResponse(template);
    }
}
