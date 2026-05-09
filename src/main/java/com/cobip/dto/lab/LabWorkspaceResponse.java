package com.cobip.dto.lab;

import java.time.LocalDateTime;

import com.cobip.domain.lab.LabWorkspace;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class LabWorkspaceResponse {

    private final Long id;
    private final String workspaceKey;
    private final String title;
    private final String language;
    private final String activeFilePath;
    private final JsonNode files;
    private final LocalDateTime lastOpenedAt;
    private final LocalDateTime updatedAt;

    private LabWorkspaceResponse(LabWorkspace workspace) {
        this.id = workspace.getId();
        this.workspaceKey = workspace.getWorkspaceKey();
        this.title = workspace.getTitle();
        this.language = workspace.getLanguage();
        this.activeFilePath = workspace.getActiveFilePath();
        this.files = workspace.getFilesJson();
        this.lastOpenedAt = workspace.getLastOpenedAt();
        this.updatedAt = workspace.getUpdatedAt();
    }

    public static LabWorkspaceResponse from(LabWorkspace workspace) {
        return new LabWorkspaceResponse(workspace);
    }
}
