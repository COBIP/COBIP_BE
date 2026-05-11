package com.cobip.dto.admin;

import com.cobip.domain.practice.TemplatePracticeFile;

import lombok.Getter;

@Getter
public class AdminTemplatePracticeFileResponse {

    private final Long id;
    private final String path;
    private final String name;
    private final String filePath;
    private final String language;
    private final String content;
    private final boolean readOnly;
    private final int orderIndex;

    private AdminTemplatePracticeFileResponse(TemplatePracticeFile file) {
        this.id = file.getId();
        this.path = file.getFilePath();
        this.name = extractName(file.getFilePath());
        this.filePath = file.getFilePath();
        this.language = file.getLanguage();
        this.content = file.getContent();
        this.readOnly = file.isReadOnly();
        this.orderIndex = file.getOrderIndex();
    }

    public static AdminTemplatePracticeFileResponse from(TemplatePracticeFile file) {
        return new AdminTemplatePracticeFileResponse(file);
    }

    private static String extractName(String path) {
        int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
    }
}
