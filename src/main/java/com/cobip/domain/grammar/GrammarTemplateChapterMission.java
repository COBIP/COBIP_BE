package com.cobip.domain.grammar;

import com.cobip.domain.common.BaseTimeEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "grammar_template_chapter_missions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarTemplateChapterMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private GrammarTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private GrammarTemplateChapter chapter;

    @Column(nullable = false, length = 120)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GrammarTemplateMissionType missionType;

    @Column(nullable = false)
    private int orderIndex;

    @Lob
    private String guideContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode validationJson;

    public static GrammarTemplateChapterMission create(
        GrammarTemplate template,
        GrammarTemplateChapter chapter,
        String title,
        String description,
        GrammarTemplateMissionType missionType,
        int orderIndex,
        String guideContent,
        JsonNode validationJson
    ) {
        return GrammarTemplateChapterMission.builder()
                .template(template)
                .chapter(chapter)
                .title(title)
                .description(description)
                .missionType(missionType)
                .orderIndex(orderIndex)
                .guideContent(guideContent)
                .validationJson(validationJson)
                .build();
    }

    public void update(
        String title,
        String description,
        GrammarTemplateMissionType missionType,
        int orderIndex,
        String guideContent,
        JsonNode validationJson
    ) {
        this.title = title;
        this.description = description;
        this.missionType = missionType;
        this.orderIndex = orderIndex;
        this.guideContent = guideContent;
        this.validationJson = validationJson;
    }
}
