package com.cobip.domain.practice;

import java.util.List;

import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeMissionProgressUpdateRequest;
import com.cobip.dto.practice.TemplatePracticeProgressResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplatePracticeService {

    private final TemplateRepository templateRepository;
    private final TemplatePracticeFileRepository fileRepository;
    private final TemplatePracticeMissionRepository missionRepository;
    private final TemplatePracticeProgressRepository progressRepository;
    private final TemplatePracticeMissionProgressRepository missionProgressRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public TemplatePracticeDetailResponse getPractice(User user, Long templateId) {
        Template template = getReadableTemplate(user, templateId);
        return TemplatePracticeDetailResponse.of(
                template,
                fileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId()),
                missionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId()),
                findProgress(user, template.getId())
        );
    }

    @Transactional
    public TemplatePracticeProgressResponse startPractice(User user, Long templateId) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission firstMission = missionRepository
                .findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId())
                .stream()
                .findFirst()
                .orElse(null);
        TemplatePracticeProgress progress = progressRepository.findByUserIdAndTemplateId(managedUser.getId(), template.getId())
                .orElseGet(() -> progressRepository.save(TemplatePracticeProgress.start(managedUser, template, firstMission)));
        progress.touch();
        return TemplatePracticeProgressResponse.from(progress);
    }

    @Transactional
    public TemplatePracticeProgressResponse updateMissionProgress(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeMissionProgressUpdateRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = missionRepository.findByIdAndTemplateId(missionId, template.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_MISSION_NOT_FOUND));

        TemplatePracticeProgress progress = progressRepository.findByUserIdAndTemplateId(managedUser.getId(), template.getId())
                .orElseGet(() -> progressRepository.save(TemplatePracticeProgress.start(managedUser, template, mission)));
        TemplatePracticeMissionProgress missionProgress = missionProgressRepository
                .findByUserIdAndMissionId(managedUser.getId(), mission.getId())
                .orElseGet(() -> missionProgressRepository.save(TemplatePracticeMissionProgress.start(managedUser, mission)));
        missionProgress.changeStatus(request.getStatus());

        long totalMissionCount = missionRepository.countByTemplateId(template.getId());
        long completedMissionCount = missionProgressRepository.countCompletedByUserAndTemplate(
                managedUser.getId(),
                template.getId(),
                TemplatePracticeMissionProgressStatus.COMPLETED
        );
        TemplatePracticeMission currentMission = request.getStatus() == TemplatePracticeMissionProgressStatus.COMPLETED
                ? missionRepository
                        .findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(
                                template.getId(),
                                mission.getOrderIndex()
                        )
                        .orElse(mission)
                : mission;
        progress.updateProgress((int) completedMissionCount, (int) totalMissionCount, currentMission);
        return TemplatePracticeProgressResponse.from(progress);
    }

    private TemplatePracticeProgress findProgress(User user, Long templateId) {
        if (user == null) {
            return null;
        }
        return progressRepository.findByUserIdAndTemplateId(user.getId(), templateId).orElse(null);
    }

    private Template getReadableTemplate(User user, Long templateId) {
        Template template = templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
        if (template.isOwner(user)) {
            return template;
        }
        if (!template.isPublic()) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.isPremium() && !subscriptionService.hasActiveSubscription(user)) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_REQUIRED);
        }
        return template;
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
