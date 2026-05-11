package com.cobip.domain.grammar;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "grammar_template_chapters")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarTemplateChapter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private GrammarTemplate template;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private Integer orderIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentJson;

    @Lob
    @Column(nullable = false)
    private String searchableText;

    private LocalDateTime deletedAt;

    public static GrammarTemplateChapter create(
        GrammarTemplate template,
        String title,
        Integer orderIndex,
        JsonNode contentJson,
        String searchableText
    ) {
        return GrammarTemplateChapter.builder()
                .template(template)
                .title(title)
                .orderIndex(orderIndex)
                .contentJson(contentJson)
                .searchableText(searchableText)
                .build();
    }

    public void update(String title, Integer orderIndex, JsonNode contentJson, String searchableText) {
        if (title != null) {
            this.title = title;
        }
        if (orderIndex != null) {
            this.orderIndex = orderIndex;
        }
        if (contentJson != null) {
            this.contentJson = contentJson;
            this.searchableText = searchableText;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
