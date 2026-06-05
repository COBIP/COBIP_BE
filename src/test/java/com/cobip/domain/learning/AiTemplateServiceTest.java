package com.cobip.domain.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.mypage.AiTemplateResponse;
import com.cobip.dto.mypage.AiTemplateSaveRequest;
import com.cobip.dto.mypage.AiTemplateUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiTemplateServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiTemplateProgressRepository aiTemplateProgressRepository;

    private AiTemplateService aiTemplateService;

    @BeforeEach
    void setUp() {
        aiTemplateService = new AiTemplateService(aiTemplateProgressRepository);
    }

    @Test
    void saveTemplateCreatesNewTemplate() {
        User user = user();
        AiTemplateSaveRequest request = saveRequest();
        AiTemplateProgress saved = AiTemplateProgress.create(
                user,
                "ai-jwt-login",
                "JWT 로그인 구현",
                objectMapper.createObjectNode().put("title", "JWT 로그인 구현"),
                null,
                null,
                0,
                null,
                0,
                false,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        when(aiTemplateProgressRepository.findByUserIdAndAiTemplateId(1L, "ai-jwt-login")).thenReturn(Optional.empty());
        when(aiTemplateProgressRepository.save(any(AiTemplateProgress.class))).thenReturn(saved);

        AiTemplateResponse response = aiTemplateService.saveTemplate(user, request);

        assertThat(response.getAiTemplateId()).isEqualTo("ai-jwt-login");
        assertThat(response.getTemplateTitle()).isEqualTo("JWT 로그인 구현");
        assertThat(response.getContentType()).isEqualTo("AI_TEMPLATE");
    }

    @Test
    void updateTemplateUpdatesProgressFields() {
        User user = user();
        AiTemplateProgress progress = AiTemplateProgress.create(
                user,
                "ai-jwt-login",
                "JWT 로그인 구현",
                objectMapper.createObjectNode().put("title", "JWT 로그인 구현"),
                null,
                null,
                0,
                null,
                0,
                false,
                LocalDateTime.now().minusDays(1)
        );
        AiTemplateUpdateRequest request = new AiTemplateUpdateRequest();
        ReflectionTestUtils.setField(request, "progressPercent", 40);
        ReflectionTestUtils.setField(request, "studySeconds", 300L);
        ReflectionTestUtils.setField(request, "lastStep", "section-2");

        when(aiTemplateProgressRepository.findByUserIdAndAiTemplateId(1L, "ai-jwt-login"))
                .thenReturn(Optional.of(progress));

        AiTemplateResponse response = aiTemplateService.updateTemplate(user, "ai-jwt-login", request);

        assertThat(response.getProgressPercent()).isEqualTo(40);
        assertThat(response.getStudySeconds()).isEqualTo(300L);
        assertThat(response.getLastStep()).isEqualTo("section-2");
    }

    @Test
    void deleteTemplateRejectsMissingTemplate() {
        User user = user();
        when(aiTemplateProgressRepository.findByUserIdAndAiTemplateId(1L, "missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiTemplateService.deleteTemplate(user, "missing"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AI_TEMPLATE_NOT_FOUND);
    }

    @Test
    void deleteTemplateDeletesOwnedTemplate() {
        User user = user();
        AiTemplateProgress progress = AiTemplateProgress.create(
                user,
                "ai-jwt-login",
                "JWT 로그인 구현",
                objectMapper.createObjectNode().put("title", "JWT 로그인 구현"),
                null,
                null,
                0,
                null,
                0,
                false,
                LocalDateTime.now()
        );
        when(aiTemplateProgressRepository.findByUserIdAndAiTemplateId(1L, "ai-jwt-login"))
                .thenReturn(Optional.of(progress));

        aiTemplateService.deleteTemplate(user, "ai-jwt-login");

        verify(aiTemplateProgressRepository).delete(progress);
    }

    private AiTemplateSaveRequest saveRequest() {
        AiTemplateSaveRequest request = new AiTemplateSaveRequest();
        ReflectionTestUtils.setField(request, "aiTemplateId", "ai-jwt-login");
        ReflectionTestUtils.setField(request, "templateTitle", "JWT 로그인 구현");
        ReflectionTestUtils.setField(request, "templateSnapshot", objectMapper.createObjectNode().put("title", "JWT 로그인 구현"));
        return request;
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
