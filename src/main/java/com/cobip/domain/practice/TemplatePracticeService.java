package com.cobip.domain.practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.practice.TemplatePracticeDetailResponse;
import com.cobip.dto.practice.TemplatePracticeMissionProgressUpdateRequest;
import com.cobip.dto.practice.TemplatePracticeProgressResponse;
import com.cobip.dto.practice.TemplatePracticeQuizSubmissionRequest;
import com.cobip.dto.practice.TemplatePracticeQuizSubmissionResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final TemplatePracticeSubmissionRepository submissionRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public TemplatePracticeDetailResponse getPractice(User user, Long templateId) {
        Template template = getReadableTemplate(user, templateId);
        List<TemplatePracticeFile> files = fileRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId());
        List<TemplatePracticeMission> missions = missionRepository.findByTemplateIdOrderByOrderIndexAscIdAsc(template.getId());
        return TemplatePracticeDetailResponse.of(
                template,
                files,
                missions,
                findProgress(user, template.getId()),
                findMissionProgresses(user, template.getId()),
                findUserContentsByFilePath(user, template.getId())
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

    @Transactional
    public TemplatePracticeQuizSubmissionResponse submitQuizMission(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeQuizSubmissionRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = missionRepository.findByIdAndTemplateId(missionId, template.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_MISSION_NOT_FOUND));

        QuizAnswer quizAnswer = quizAnswer(mission.getValidationJson());
        boolean correct = isCorrectAnswer(quizAnswer.type(), quizAnswer.answer(), request.getAnswer());
        TemplatePracticeProgress progress = progressRepository.findByUserIdAndTemplateId(
                managedUser.getId(),
                template.getId()
        ).orElseGet(() -> progressRepository.save(TemplatePracticeProgress.start(managedUser, template, mission)));
        TemplatePracticeMissionProgress missionProgress = missionProgressRepository
                .findByUserIdAndMissionId(managedUser.getId(), mission.getId())
                .orElseGet(() -> missionProgressRepository.save(TemplatePracticeMissionProgress.start(managedUser, mission)));

        if (correct) {
            missionProgress.changeStatus(TemplatePracticeMissionProgressStatus.COMPLETED);
            refreshPracticeProgress(managedUser, template, mission, progress, TemplatePracticeMissionProgressStatus.COMPLETED);
        } else {
            missionProgress.changeStatus(TemplatePracticeMissionProgressStatus.IN_PROGRESS);
        }
        syncLearningProgress(managedUser, template, mission, progress.getProgressPercent(), correct);

        return TemplatePracticeQuizSubmissionResponse.of(correct, quizAnswer.explanation(), progress.getProgressPercent());
    }

    private TemplatePracticeProgress findProgress(User user, Long templateId) {
        if (user == null) {
            return null;
        }
        return progressRepository.findByUserIdAndTemplateId(user.getId(), templateId).orElse(null);
    }

    private List<TemplatePracticeMissionProgress> findMissionProgresses(User user, Long templateId) {
        if (user == null) {
            return List.of();
        }
        return missionProgressRepository.findByUserIdAndTemplateId(user.getId(), templateId);
    }

    private Map<String, String> findUserContentsByFilePath(User user, Long templateId) {
        if (user == null) {
            return Map.of();
        }

        Map<String, String> userContentsByFilePath = new LinkedHashMap<>();
        submissionRepository.findByUserIdAndTemplateIdOrderByCreatedAtDescIdDesc(user.getId(), templateId)
                .forEach(submission -> putProjectFilesIfAbsent(
                        userContentsByFilePath,
                        submission.getSourceCode()
                ));
        return userContentsByFilePath;
    }

    private void putProjectFilesIfAbsent(Map<String, String> userContentsByFilePath, String sourceCode) {
        try {
            JsonNode filesNode = objectMapper.readTree(sourceCode);
            if (filesNode == null || !filesNode.isArray()) {
                return;
            }

            for (JsonNode fileNode : filesNode) {
                JsonNode filePathNode = fileNode.get("filePath");
                JsonNode contentNode = fileNode.get("content");
                if (filePathNode == null || contentNode == null || !filePathNode.isTextual() || !contentNode.isTextual()) {
                    continue;
                }
                userContentsByFilePath.putIfAbsent(filePathNode.asText(), contentNode.asText());
            }
        } catch (JsonProcessingException e) {
            // Single-file submissions are stored as raw source code, so they cannot be mapped to a file path here.
        }
    }

    private void refreshPracticeProgress(
        User user,
        Template template,
        TemplatePracticeMission mission,
        TemplatePracticeProgress progress,
        TemplatePracticeMissionProgressStatus missionStatus
    ) {
        long totalMissionCount = missionRepository.countByTemplateId(template.getId());
        long completedMissionCount = missionProgressRepository.countCompletedByUserAndTemplate(
                user.getId(),
                template.getId(),
                TemplatePracticeMissionProgressStatus.COMPLETED
        );
        TemplatePracticeMission currentMission = missionStatus == TemplatePracticeMissionProgressStatus.COMPLETED
                ? missionRepository
                        .findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(
                                template.getId(),
                                mission.getOrderIndex()
                        )
                        .orElse(mission)
                : mission;
        progress.updateProgress((int) completedMissionCount, (int) totalMissionCount, currentMission);
    }

    private void syncLearningProgress(
        User user,
        Template template,
        TemplatePracticeMission mission,
        int progressPercent,
        boolean correct
    ) {
        LearningProgress learningProgress = learningProgressRepository.findByUserIdAndTemplateId(user.getId(), template.getId())
                .orElseGet(() -> learningProgressRepository.save(LearningProgress.start(user, template)));
        learningProgress.recordQuizSubmission(progressPercent, mission.getTitle(), correct);
    }

    private QuizAnswer quizAnswer(JsonNode validationJson) {
        if (validationJson == null || validationJson.isNull()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        String answer = textValue(validationJson, "answer");
        if (answer == null || answer.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return new QuizAnswer(
                textValue(validationJson, "type"),
                answer,
                textValue(validationJson, "explanation")
        );
    }

    private boolean isCorrectAnswer(String type, String expectedAnswer, String submittedAnswer) {
        if ("multiple_choice".equals(normalizeType(type))) {
            return expectedAnswer.equals(submittedAnswer);
        }
        return normalizeAnswer(expectedAnswer).equals(normalizeAnswer(submittedAnswer));
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim()
                .replace("-", "_")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeAnswer(String answer) {
        return answer.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private String textValue(JsonNode jsonNode, String fieldName) {
        JsonNode value = jsonNode.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
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

    private record QuizAnswer(String type, String answer, String explanation) {
    }
}
