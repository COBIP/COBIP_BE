package com.cobip.dto.practice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cobip.domain.practice.TemplatePracticeFile;
import com.cobip.domain.practice.TemplatePracticeMission;
import com.cobip.domain.practice.TemplatePracticeMissionProgress;
import com.cobip.domain.practice.TemplatePracticeMissionProgressStatus;
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
        TemplatePracticeProgress progress,
        List<TemplatePracticeMissionProgress> missionProgresses,
        Map<String, String> userContentsByFilePath
    ) {
        Map<Long, TemplatePracticeMissionProgressStatus> progressStatusByMissionId = missionProgresses.stream()
                .collect(Collectors.toMap(
                        missionProgress -> missionProgress.getMission().getId(),
                        TemplatePracticeMissionProgress::getStatus,
                        (left, right) -> left
                ));

        this.templateId = template.getId();
        this.templateTitle = template.getTitle();
        this.files = files.stream()
                .map(file -> TemplatePracticeFileResponse.from(file, userContentsByFilePath.get(file.getFilePath())))
                .toList();
        this.missions = missions.stream()
                .map(mission -> TemplatePracticeMissionResponse.from(
                        mission,
                        progressStatusByMissionId.get(mission.getId())
                ))
                .toList();
        this.progress = TemplatePracticeProgressResponse.from(progress);
    }

    public static TemplatePracticeDetailResponse of(
        Template template,
        List<TemplatePracticeFile> files,
        List<TemplatePracticeMission> missions,
        TemplatePracticeProgress progress
    ) {
        return of(template, files, missions, progress, List.of(), Map.of());
    }

    public static TemplatePracticeDetailResponse of(
        Template template,
        List<TemplatePracticeFile> files,
        List<TemplatePracticeMission> missions,
        TemplatePracticeProgress progress,
        List<TemplatePracticeMissionProgress> missionProgresses,
        Map<String, String> userContentsByFilePath
    ) {
        return new TemplatePracticeDetailResponse(
                template,
                files,
                missions,
                progress,
                missionProgresses,
                userContentsByFilePath
        );
    }
}
