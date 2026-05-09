package com.cobip.domain.practice;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeFileRequest;
import com.cobip.dto.practice.TemplatePracticeFileResponse;
import com.cobip.dto.practice.TemplatePracticeMissionRequest;
import com.cobip.dto.practice.TemplatePracticeMissionResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTemplatePracticeService {

    private final TemplateRepository templateRepository;
    private final TemplatePracticeFileRepository fileRepository;
    private final TemplatePracticeMissionRepository missionRepository;

    @Transactional(readOnly = true)
    public TemplatePracticeDetailResponse getPractice(Long templateId) {
        Template template = findTemplate(templateId);
        return TemplatePracticeDetailResponse.of(
                template,
                fileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId()),
                missionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId()),
                null
        );
    }

    @Transactional
    public TemplatePracticeFileResponse createFile(Long templateId, TemplatePracticeFileRequest request) {
        Template template = findTemplate(templateId);
        TemplatePracticeFile file = TemplatePracticeFile.builder()
                .template(template)
                .filePath(request.getFilePath())
                .language(request.getLanguage())
                .content(request.getContent())
                .readOnly(request.getReadOnly())
                .orderIndex(request.getOrderIndex())
                .build();
        return TemplatePracticeFileResponse.from(fileRepository.save(file));
    }

    @Transactional
    public TemplatePracticeFileResponse updateFile(
        Long templateId,
        Long fileId,
        TemplatePracticeFileRequest request
    ) {
        findTemplate(templateId);
        TemplatePracticeFile file = fileRepository.findByIdAndTemplateId(fileId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_FILE_NOT_FOUND));
        file.update(
                request.getFilePath(),
                request.getLanguage(),
                request.getContent(),
                request.getReadOnly(),
                request.getOrderIndex()
        );
        return TemplatePracticeFileResponse.from(file);
    }

    @Transactional
    public void deleteFile(Long templateId, Long fileId) {
        findTemplate(templateId);
        TemplatePracticeFile file = fileRepository.findByIdAndTemplateId(fileId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_FILE_NOT_FOUND));
        fileRepository.delete(file);
    }

    @Transactional
    public TemplatePracticeMissionResponse createMission(Long templateId, TemplatePracticeMissionRequest request) {
        Template template = findTemplate(templateId);
        TemplatePracticeMission mission = TemplatePracticeMission.builder()
                .template(template)
                .title(request.getTitle())
                .description(request.getDescription())
                .missionType(request.getMissionType())
                .orderIndex(request.getOrderIndex())
                .guideContent(request.getGuideContent())
                .validationJson(request.getValidationJson())
                .build();
        return TemplatePracticeMissionResponse.from(missionRepository.save(mission));
    }

    @Transactional
    public TemplatePracticeMissionResponse updateMission(
        Long templateId,
        Long missionId,
        TemplatePracticeMissionRequest request
    ) {
        findTemplate(templateId);
        TemplatePracticeMission mission = missionRepository.findByIdAndTemplateId(missionId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_MISSION_NOT_FOUND));
        mission.update(
                request.getTitle(),
                request.getDescription(),
                request.getMissionType(),
                request.getOrderIndex(),
                request.getGuideContent(),
                request.getValidationJson()
        );
        return TemplatePracticeMissionResponse.from(mission);
    }

    @Transactional
    public void deleteMission(Long templateId, Long missionId) {
        findTemplate(templateId);
        TemplatePracticeMission mission = missionRepository.findByIdAndTemplateId(missionId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_MISSION_NOT_FOUND));
        missionRepository.delete(mission);
    }

    private Template findTemplate(Long templateId) {
        return templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    }
}
