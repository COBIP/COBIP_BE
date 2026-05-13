package com.cobip.domain.practice;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.practice.TemplatePracticeCodeRunRequest;
import com.cobip.dto.practice.TemplatePracticeCodeRunResponse;
import com.cobip.dto.practice.TemplatePracticeProjectExecutionRequest;
import com.cobip.dto.practice.TemplatePracticeProjectRunResponse;
import com.cobip.dto.practice.TemplatePracticeSubmissionRequest;
import com.cobip.dto.practice.TemplatePracticeSubmissionResponse;
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
public class TemplatePracticeExecutionService {

    private static final int DEFAULT_TIME_LIMIT_MILLIS = 5000;
    private static final int DEFAULT_MEMORY_LIMIT_MB = 128;
    private static final int DEFAULT_PROJECT_MEMORY_LIMIT_MB = 1024;
    private static final int PROJECT_RUN_MAX_TIME_LIMIT_MILLIS = 60000;
    private static final int PROJECT_SUBMISSION_MAX_TIME_LIMIT_MILLIS = 60000;
    private static final String DEFAULT_PROJECT_IMAGE = "gradle:8.14-jdk21";
    private static final String DEFAULT_PROJECT_COMMAND = "gradle test --no-daemon";

    private final TemplateRepository templateRepository;
    private final TemplatePracticeMissionRepository missionRepository;
    private final TemplatePracticeProgressRepository progressRepository;
    private final TemplatePracticeMissionProgressRepository missionProgressRepository;
    private final TemplatePracticeSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final CodeExecutionClient codeExecutionClient;
    private final ProjectExecutionClient projectExecutionClient;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public TemplatePracticeCodeRunResponse runMission(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeCodeRunRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = getMission(template.getId(), missionId);
        JsonNode validationJson = mission.getValidationJson();

        CodeExecutionResult result = codeExecutionClient.execute(
                request.getLanguage(),
                request.getSourceCode(),
                normalizeInput(request.getInput()),
                null,
                intValue(validationJson, "timeLimitMillis", DEFAULT_TIME_LIMIT_MILLIS),
                intValue(validationJson, "memoryLimitMb", DEFAULT_MEMORY_LIMIT_MB)
        );
        return TemplatePracticeCodeRunResponse.from(result);
    }

    @Transactional
    public TemplatePracticeSubmissionResponse submitMission(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeSubmissionRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = getMission(template.getId(), missionId);
        List<ValidationCase> validationCases = validationCases(mission.getValidationJson());

        int passedCount = 0;
        TemplatePracticeSubmissionStatus finalStatus = TemplatePracticeSubmissionStatus.ACCEPTED;
        CodeExecutionResult lastResult = null;

        for (ValidationCase validationCase : validationCases) {
            CodeExecutionResult result = codeExecutionClient.execute(
                    request.getLanguage(),
                    request.getSourceCode(),
                    validationCase.input(),
                    validationCase.expectedOutput(),
                    intValue(mission.getValidationJson(), "timeLimitMillis", DEFAULT_TIME_LIMIT_MILLIS),
                    intValue(mission.getValidationJson(), "memoryLimitMb", DEFAULT_MEMORY_LIMIT_MB)
            );
            lastResult = result;

            if (result.status() == CodingSubmissionStatus.ACCEPTED) {
                passedCount++;
                continue;
            }

            finalStatus = TemplatePracticeSubmissionStatus.from(result.status());
            break;
        }

        TemplatePracticeSubmission submission = submissionRepository.save(TemplatePracticeSubmission.builder()
                .user(managedUser)
                .template(template)
                .mission(mission)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .status(finalStatus)
                .passedCount(passedCount)
                .totalCount(validationCases.size())
                .judgeToken(lastResult == null ? null : lastResult.token())
                .stdout(lastResult == null ? null : lastResult.stdout())
                .stderr(lastResult == null ? null : lastResult.stderr())
                .compileOutput(lastResult == null ? null : lastResult.compileOutput())
                .build());

        updateProgressAfterSubmission(managedUser, template, mission, finalStatus);
        return TemplatePracticeSubmissionResponse.of(submission, lastResult);
    }

    @Transactional(readOnly = true)
    public TemplatePracticeProjectRunResponse runProjectMission(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeProjectExecutionRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = getMission(template.getId(), missionId);
        ProjectExecutionResult result = projectExecutionClient.execute(projectRequest(
                mission.getValidationJson(),
                request,
                "runCommand",
                PROJECT_RUN_MAX_TIME_LIMIT_MILLIS
        ));
        return TemplatePracticeProjectRunResponse.from(result);
    }

    @Transactional
    public TemplatePracticeSubmissionResponse submitProjectMission(
        User user,
        Long templateId,
        Long missionId,
        TemplatePracticeProjectExecutionRequest request
    ) {
        User managedUser = getManagedUser(user);
        Template template = getReadableTemplate(managedUser, templateId);
        TemplatePracticeMission mission = getMission(template.getId(), missionId);
        ProjectExecutionResult result = projectExecutionClient.execute(projectRequest(
                mission.getValidationJson(),
                request,
                "testCommand",
                PROJECT_SUBMISSION_MAX_TIME_LIMIT_MILLIS
        ));

        TemplatePracticeSubmissionStatus finalStatus = result.status();
        TemplatePracticeSubmission submission = submissionRepository.save(TemplatePracticeSubmission.builder()
                .user(managedUser)
                .template(template)
                .mission(mission)
                .language(projectLanguage(mission.getValidationJson()))
                .sourceCode(projectSourcePayload(request))
                .status(finalStatus)
                .passedCount(finalStatus == TemplatePracticeSubmissionStatus.ACCEPTED ? 1 : 0)
                .totalCount(1)
                .stdout(result.stdout())
                .stderr(result.stderr())
                .build());

        updateProgressAfterSubmission(managedUser, template, mission, finalStatus);
        return TemplatePracticeSubmissionResponse.ofProject(submission, result);
    }

    private void updateProgressAfterSubmission(
        User user,
        Template template,
        TemplatePracticeMission mission,
        TemplatePracticeSubmissionStatus status
    ) {
        TemplatePracticeProgress progress = progressRepository.findByUserIdAndTemplateId(user.getId(), template.getId())
                .orElseGet(() -> progressRepository.save(TemplatePracticeProgress.start(user, template, mission)));
        TemplatePracticeMissionProgress missionProgress = missionProgressRepository
                .findByUserIdAndMissionId(user.getId(), mission.getId())
                .orElseGet(() -> missionProgressRepository.save(TemplatePracticeMissionProgress.start(user, mission)));

        if (status != TemplatePracticeSubmissionStatus.ACCEPTED) {
            progress.touch();
            missionProgress.changeStatus(TemplatePracticeMissionProgressStatus.IN_PROGRESS);
            return;
        }

        missionProgress.changeStatus(TemplatePracticeMissionProgressStatus.COMPLETED);
        long totalMissionCount = missionRepository.countByTemplateId(template.getId());
        long completedMissionCount = missionProgressRepository.countCompletedByUserAndTemplate(
                user.getId(),
                template.getId(),
                TemplatePracticeMissionProgressStatus.COMPLETED
        );
        TemplatePracticeMission currentMission = missionRepository
                .findFirstByTemplateIdAndOrderIndexGreaterThanOrderByOrderIndexAscIdAsc(
                        template.getId(),
                        mission.getOrderIndex()
                )
                .orElse(mission);
        progress.updateProgress((int) completedMissionCount, (int) totalMissionCount, currentMission);
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

    private TemplatePracticeMission getMission(Long templateId, Long missionId) {
        return missionRepository.findByIdAndTemplateId(missionId, templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_PRACTICE_MISSION_NOT_FOUND));
    }

    private ProjectExecutionRequest projectRequest(
        JsonNode validationJson,
        TemplatePracticeProjectExecutionRequest request,
        String commandField,
        int maxTimeLimitMillis
    ) {
        String command = textValue(validationJson, commandField, DEFAULT_PROJECT_COMMAND);
        String dockerImage = textValue(validationJson, "dockerImage", DEFAULT_PROJECT_IMAGE);
        return new ProjectExecutionRequest(
                request.getFiles().stream()
                        .map(file -> new ProjectExecutionFile(file.getFilePath(), file.getContent()))
                        .toList(),
                command,
                dockerImage,
                cappedIntValue(validationJson, "timeLimitMillis", maxTimeLimitMillis, maxTimeLimitMillis),
                intValue(validationJson, "memoryLimitMb", DEFAULT_PROJECT_MEMORY_LIMIT_MB)
        );
    }

    private CodingLanguage projectLanguage(JsonNode validationJson) {
        String language = textValue(validationJson, "language", CodingLanguage.JAVA.name());
        try {
            return CodingLanguage.valueOf(language);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, e);
        }
    }

    private String projectSourcePayload(TemplatePracticeProjectExecutionRequest request) {
        try {
            return objectMapper.writeValueAsString(request.getFiles());
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, e);
        }
    }

    private User getManagedUser(User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private List<ValidationCase> validationCases(JsonNode validationJson) {
        if (validationJson == null || validationJson.isNull()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        JsonNode testCasesNode = validationJson.get("testCases");
        if (testCasesNode != null && testCasesNode.isArray()) {
            List<ValidationCase> validationCases = new ArrayList<>();
            for (JsonNode testCaseNode : testCasesNode) {
                validationCases.add(validationCase(testCaseNode));
            }
            if (!validationCases.isEmpty()) {
                return validationCases;
            }
        }

        String expectedOutput = requiredText(validationJson, "expectedOutput");
        return List.of(new ValidationCase(textValue(validationJson, "input", ""), expectedOutput));
    }

    private ValidationCase validationCase(JsonNode testCaseNode) {
        return new ValidationCase(
                textValue(testCaseNode, "input", ""),
                requiredText(testCaseNode, "expectedOutput")
        );
    }

    private String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return value.asText();
    }

    private String textValue(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asText();
    }

    private int intValue(JsonNode node, String fieldName, int defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asInt(defaultValue);
    }

    private int cappedIntValue(JsonNode node, String fieldName, int defaultValue, int maxValue) {
        return Math.max(1, Math.min(intValue(node, fieldName, defaultValue), maxValue));
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input;
    }

    private record ValidationCase(String input, String expectedOutput) {
    }
}
