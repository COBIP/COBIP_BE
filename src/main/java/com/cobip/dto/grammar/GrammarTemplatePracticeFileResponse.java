package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplatePracticeFile;
import com.cobip.domain.grammar.GrammarTemplatePracticeFileType;

import lombok.Getter;

@Getter
public class GrammarTemplatePracticeFileResponse {

    private final Long id;
    private final Long templateId;
    private final Long chapterId;
    private final GrammarTemplatePracticeFileType nodeType;
    private final String filePath;
    private final String language;
    private final String content;
    private final boolean readOnly;
    private final int orderIndex;

    private GrammarTemplatePracticeFileResponse(GrammarTemplatePracticeFile file) {
        this.id = file.getId();
        this.templateId = file.getTemplate().getId();
        this.chapterId = file.getChapter().getId();
        this.nodeType = file.getNodeType();
        this.filePath = file.getFilePath();
        this.language = file.getLanguage();
        this.content = file.getContent();
        this.readOnly = file.isReadOnly();
        this.orderIndex = file.getOrderIndex();
    }

    public static GrammarTemplatePracticeFileResponse from(GrammarTemplatePracticeFile file) {
        return new GrammarTemplatePracticeFileResponse(file);
    }
}
