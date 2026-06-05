package com.cobip.domain.learning;

import com.cobip.domain.user.User;
import com.cobip.dto.mypage.AiTemplateResponse;
import com.cobip.dto.mypage.AiTemplateSaveRequest;
import com.cobip.dto.mypage.AiTemplateUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiTemplateService {

    private final AiTemplateProgressRepository aiTemplateProgressRepository;

    @Transactional
    public AiTemplateResponse saveTemplate(User user, AiTemplateSaveRequest request) {
        AiTemplateProgress progress = aiTemplateProgressRepository.findByUserIdAndAiTemplateId(
                user.getId(),
                request.getAiTemplateId()
        ).orElseGet(() -> AiTemplateProgress.create(
                user,
                request.getAiTemplateId(),
                request.getTemplateTitle(),
                request.getTemplateSnapshot(),
                request.getSections(),
                request.getLastLearningPosition(),
                request.getProgressPercentOrDefault(),
                request.getLastStep(),
                request.getStudySecondsOrDefault(),
                request.getCompleted(),
                request.getLastAccessedAt()
        ));

        if (progress.getId() != null) {
            progress.overwrite(
                    request.getTemplateTitle(),
                    request.getTemplateSnapshot(),
                    request.getSections(),
                    request.getLastLearningPosition(),
                    request.getProgressPercentOrDefault(),
                    request.getLastStep(),
                    request.getStudySecondsOrDefault(),
                    request.getCompleted(),
                    request.getLastAccessedAt()
            );
        }

        return AiTemplateResponse.from(aiTemplateProgressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public PageResponse<AiTemplateResponse> getTemplates(User user, Pageable pageable) {
        return PageResponse.from(aiTemplateProgressRepository
                .findByUserIdOrderByLastAccessedAtDescIdDesc(user.getId(), pageable)
                .map(AiTemplateResponse::from));
    }

    @Transactional(readOnly = true)
    public AiTemplateResponse getTemplate(User user, String aiTemplateId) {
        return AiTemplateResponse.from(findTemplate(user, aiTemplateId));
    }

    @Transactional
    public AiTemplateResponse updateTemplate(User user, String aiTemplateId, AiTemplateUpdateRequest request) {
        AiTemplateProgress progress = findTemplate(user, aiTemplateId);
        progress.update(
                request.getTemplateTitle(),
                request.getTemplateSnapshot(),
                request.getSections(),
                request.getLastLearningPosition(),
                request.getProgressPercent(),
                request.getLastStep(),
                request.getStudySeconds(),
                request.getCompleted(),
                request.getLastAccessedAt()
        );
        return AiTemplateResponse.from(progress);
    }

    @Transactional
    public void deleteTemplate(User user, String aiTemplateId) {
        aiTemplateProgressRepository.delete(findTemplate(user, aiTemplateId));
    }

    private AiTemplateProgress findTemplate(User user, String aiTemplateId) {
        return aiTemplateProgressRepository.findByUserIdAndAiTemplateId(user.getId(), aiTemplateId)
                .orElseThrow(() -> new CustomException(ErrorCode.AI_TEMPLATE_NOT_FOUND));
    }
}
