package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateMediaType;

import lombok.Getter;

@Getter
public class GrammarTemplateMediaUploadResponse {

    private final Long templateId;
    private final GrammarTemplateMediaType type;
    private final String fileKey;
    private final String fileUrl;
    private final String contentType;

    public GrammarTemplateMediaUploadResponse(
        Long templateId,
        GrammarTemplateMediaType type,
        String fileKey,
        String fileUrl,
        String contentType
    ) {
        this.templateId = templateId;
        this.type = type;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
    }
}
