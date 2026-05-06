package com.cobip.domain.moderation;

import java.time.LocalDateTime;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateRepository;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminContentModerationRequest;
import com.cobip.dto.admin.AdminContentModerationResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContentModerationService {

    private final TemplateRepository templateRepository;
    private final GrammarTemplateRepository grammarTemplateRepository;
    private final ActivityHistoryService activityHistoryService;

    @Transactional
    public AdminContentModerationResponse moderate(AdminContentModerationRequest request, User adminUser) {
        String resultStatus = switch (request.getTargetType()) {
            case TEMPLATE -> moderateTemplate(request.getTargetId(), request.getAction());
            case GRAMMAR_TEMPLATE -> moderateGrammarTemplate(request.getTargetId(), request.getAction());
        };

        if (adminUser != null) {
            activityHistoryService.record(
                    adminUser,
                    ActivityType.CONTENT_MODERATED,
                    moderationMessage(request),
                    request.getTargetType().name(),
                    request.getTargetId()
            );
        }

        return AdminContentModerationResponse.of(
                request,
                resultStatus,
                adminUser,
                LocalDateTime.now()
        );
    }

    private String moderateTemplate(Long templateId, ContentModerationAction action) {
        Template template = templateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (action == ContentModerationAction.DELETE) {
            template.delete();
            return "DELETED";
        }

        template.changeExposure(TemplateVisibility.PRIVATE, template.getAccessLevel());
        return template.getVisibility().name();
    }

    private String moderateGrammarTemplate(Long templateId, ContentModerationAction action) {
        GrammarTemplate template = grammarTemplateRepository.findByIdAndDeletedAtIsNull(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.GRAMMAR_TEMPLATE_NOT_FOUND));

        if (action == ContentModerationAction.DELETE) {
            template.delete();
            return "DELETED";
        }

        template.changeStatus(GrammarTemplateStatus.ARCHIVED);
        return template.getStatus().name();
    }

    private String moderationMessage(AdminContentModerationRequest request) {
        return "Admin moderated content: " + request.getAction() + ".";
    }
}
