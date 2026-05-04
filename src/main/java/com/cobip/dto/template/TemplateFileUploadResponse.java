package com.cobip.dto.template;

import lombok.Getter;

@Getter
public class TemplateFileUploadResponse {

    private final Long templateId;
    private final String fileKey;
    private final String fileUrl;

    public TemplateFileUploadResponse(Long templateId, String fileKey, String fileUrl) {
        this.templateId = templateId;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }
}
