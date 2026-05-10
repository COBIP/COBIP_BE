package com.cobip.domain.grammar;

import com.cobip.domain.common.BaseTimeEntity;

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

@Entity
@Table(name = "grammar_template_practice_files")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarTemplatePracticeFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private GrammarTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private GrammarTemplateChapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrammarTemplatePracticeFileType nodeType;

    @Column(nullable = false, length = 255)
    private String filePath;

    @Column(length = 40)
    private String language;

    @Lob
    private String content;

    @Column(nullable = false)
    private boolean readOnly;

    @Column(nullable = false)
    private int orderIndex;

    public static GrammarTemplatePracticeFile create(
        GrammarTemplate template,
        GrammarTemplateChapter chapter,
        GrammarTemplatePracticeFileType nodeType,
        String filePath,
        String language,
        String content,
        boolean readOnly,
        int orderIndex
    ) {
        return GrammarTemplatePracticeFile.builder()
                .template(template)
                .chapter(chapter)
                .nodeType(nodeType)
                .filePath(filePath)
                .language(language)
                .content(content)
                .readOnly(readOnly)
                .orderIndex(orderIndex)
                .build();
    }

    public void update(
        GrammarTemplatePracticeFileType nodeType,
        String filePath,
        String language,
        String content,
        boolean readOnly,
        int orderIndex
    ) {
        this.nodeType = nodeType;
        this.filePath = filePath;
        this.language = language;
        this.content = content;
        this.readOnly = readOnly;
        this.orderIndex = orderIndex;
    }
}
