package com.cobip.domain.grammar;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "grammar_templates")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GrammarTemplateLanguage language;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GrammarTemplateDifficulty difficulty;

    @Column(nullable = false, length = 500)
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentJson;

    @Lob
    @Column(nullable = false)
    private String searchableText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrammarTemplateStatus status;

    private LocalDateTime deletedAt;

    public static GrammarTemplate create(
        String slug,
        String title,
        GrammarTemplateLanguage language,
        String category,
        GrammarTemplateDifficulty difficulty,
        String summary,
        JsonNode contentJson,
        GrammarTemplateStatus status,
        String searchableText
    ) {
        GrammarTemplateStatus templateStatus = status == null
                ? GrammarTemplateStatus.DRAFT
                : status;

        return GrammarTemplate.builder()
                .slug(slug)
                .title(title)
                .language(language)
                .category(category)
                .difficulty(difficulty)
                .summary(summary)
                .contentJson(contentJson)
                .searchableText(searchableText)
                .status(templateStatus)
                .build();
    }

    public void update(
        String slug,
        String title,
        GrammarTemplateLanguage language,
        String category,
        GrammarTemplateDifficulty difficulty,
        String summary,
        JsonNode contentJson,
        String searchableText
    ) {
        if (slug != null) {
            this.slug = slug;
        }
        if (title != null) {
            this.title = title;
        }
        if (language != null) {
            this.language = language;
        }
        if (category != null) {
            this.category = category;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (summary != null) {
            this.summary = summary;
        }
        if (contentJson != null) {
            this.contentJson = contentJson;
            this.searchableText = searchableText;
        }
    }

    public void changeStatus(GrammarTemplateStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
