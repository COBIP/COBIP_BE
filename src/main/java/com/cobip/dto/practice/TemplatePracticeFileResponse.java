package com.cobip.dto.practice;

import com.cobip.domain.practice.TemplatePracticeFile;

import lombok.Getter;

@Getter
public class TemplatePracticeFileResponse {

    private final Long id;
    private final String filePath;
    private final String language;
    private final String content;
    private final String userContent;
    private final boolean readOnly;
    private final int orderIndex;

    private TemplatePracticeFileResponse(TemplatePracticeFile file, String userContent) {
        this.id = file.getId();
        this.filePath = file.getFilePath();
        this.language = file.getLanguage();
        this.content = file.getContent();
        this.userContent = userContent;
        this.readOnly = file.isReadOnly();
        this.orderIndex = file.getOrderIndex();
    }

    public static TemplatePracticeFileResponse from(TemplatePracticeFile file) {
        return from(file, null);
    }

    public static TemplatePracticeFileResponse from(TemplatePracticeFile file, String userContent) {
        return new TemplatePracticeFileResponse(file, userContent);
    }
}
