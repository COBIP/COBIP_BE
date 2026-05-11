package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cobip.dto.auth.EmailVerificationConfirmRequest;
import com.cobip.dto.auth.EmailVerificationSendRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.cobip.infra.mail.EmailService;
import com.cobip.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private EmailService emailService;

    private RecordingTaskExecutor mailTaskExecutor;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        mailTaskExecutor = new RecordingTaskExecutor();
        emailVerificationService = new EmailVerificationService(
                userRepository,
                redisService,
                emailService,
                mailTaskExecutor
        );
        ReflectionTestUtils.setField(emailVerificationService, "codeExpirationMillis", 300_000L);
        ReflectionTestUtils.setField(emailVerificationService, "verifiedExpirationMillis", 1_800_000L);
    }

    @Test
    void sendCodeStoresCodeAndDispatchesEmail() {
        EmailVerificationSendRequest request = new EmailVerificationSendRequest();
        ReflectionTestUtils.setField(request, "email", "USER@example.com");

        emailVerificationService.sendCode(request);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        InOrder inOrder = inOrder(redisService);
        inOrder.verify(redisService).deleteVerifiedEmail("user@example.com");
        inOrder.verify(redisService).saveEmailVerificationCode(eq("user@example.com"), codeCaptor.capture(), eq(300_000L));
        verifyNoInteractions(emailService);

        mailTaskExecutor.runPending();

        verify(emailService).sendVerificationCode("user@example.com", codeCaptor.getValue());
        assertThat(codeCaptor.getValue()).matches("\\d{6}");
    }

    @Test
    void sendCodeRejectsDuplicatedEmail() {
        EmailVerificationSendRequest request = new EmailVerificationSendRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> emailVerificationService.sendCode(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void confirmCodeMarksEmailAsVerified() {
        EmailVerificationConfirmRequest request = new EmailVerificationConfirmRequest();
        ReflectionTestUtils.setField(request, "email", "USER@example.com");
        ReflectionTestUtils.setField(request, "code", "123456");
        when(redisService.matchesEmailVerificationCode("user@example.com", "123456")).thenReturn(true);

        emailVerificationService.confirmCode(request);

        verify(redisService).deleteEmailVerificationCode("user@example.com");
        verify(redisService).saveVerifiedEmail("user@example.com", 1_800_000L);
    }

    @Test
    void confirmCodeRejectsInvalidCode() {
        EmailVerificationConfirmRequest request = new EmailVerificationConfirmRequest();
        ReflectionTestUtils.setField(request, "email", "user@example.com");
        ReflectionTestUtils.setField(request, "code", "123456");

        assertThatThrownBy(() -> emailVerificationService.confirmCode(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_FAILED);
    }

    private static class RecordingTaskExecutor implements TaskExecutor {

        private Runnable pending;

        @Override
        public void execute(Runnable task) {
            pending = task;
        }

        private void runPending() {
            assertThat(pending).isNotNull();
            pending.run();
            pending = null;
        }
    }
}
