package com.cobip.dto.practice;

import java.util.List;

import com.cobip.domain.practice.TemplatePracticeFile;
import com.cobip.domain.practice.TemplatePracticeMission;
import com.cobip.domain.practice.TemplatePracticeProgress;
import com.cobip.domain.template.Template;

import lombok.Getter;

@Getter
public class TemplatePracticeDetailResponse {

    private final Long templateId;
    private final String templateTitle;
    private final List<TemplatePracticeFileResponse> files;
    private final List<TemplatePracticeMissionResponse> missions;
    private final TemplatePracticeProgressResponse progress;

    private TemplatePracticeDetailResponse(
        Template template,
        List<TemplatePracticeFile> files,
        List<TemplatePracticeMission> missions,
        TemplatePracticeProgress progress
    ) {
        this.templateId = template.getId();
        this.templateTitle = template.getTitle();
        this.files = files.stream().map(TemplatePracticeFileResponse::from).toList();
        this.missions = missions.stream().map(TemplatePracticeMissionResponse::from).toList();
        this.progress = TemplatePracticeProgressResponse.from(progress);
    }

    public static TemplatePracticeDetailResponse of(
        Template template,
        List<TemplatePracticeFile> files,
        List<TemplatePracticeMission> missions,
        TemplatePracticeProgress progress
    ) {
        return new TemplatePracticeDetailResponse(template, files, missions, progress);
    }
}
