package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.activity.ActivityHistory;
import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.learning.GrammarLearningProgressRepository;
import com.cobip.domain.learning.GrammarLearningProgressService;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.learning.UserLearningDailyStatRepository;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.template.TemplateFavoriteRepository;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.dto.user.UserWithdrawalRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserWithdrawalServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFavoriteRepository templateFavoriteRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private GrammarLearningProgressRepository grammarLearningProgressRepository;

    @Mock
    private GrammarLearningProgressService grammarLearningProgressService;

    @Mock
    private UserLearningDailyStatRepository userLearningDailyStatRepository;

    @Mock
    private ActivityHistoryRepository activityHistoryRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageService(
                userRepository,
                templateRepository,
                templateFavoriteRepository,
                learningProgressRepository,
                grammarLearningProgressRepository,
                grammarLearningProgressService,
                userLearningDailyStatRepository,
                activityHistoryRepository,
                subscriptionRepository,
                passwordEncoder
        );
    }

    @Test
    void withdrawMarksUserDeletedAndRecordsReason() {
        User user = user(UserStatus.ACTIVE);
        UserWithdrawalRequest request = request("Password1!", "  더 이상 사용하지 않음  ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded-password")).thenReturn(true);

        myPageService.withdraw(user, request);

        ArgumentCaptor<ActivityHistory> historyCaptor = ArgumentCaptor.forClass(ActivityHistory.class);
        verify(activityHistoryRepository).save(historyCaptor.capture());
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(historyCaptor.getValue().getType()).isEqualTo(ActivityType.USER_WITHDRAWN);
        assertThat(historyCaptor.getValue().getMessage()).isEqualTo("회원 탈퇴: 더 이상 사용하지 않음");
        assertThat(historyCaptor.getValue().getTargetType()).isEqualTo("USER");
        assertThat(historyCaptor.getValue().getTargetId()).isEqualTo(1L);
    }

    @Test
    void withdrawRejectsInvalidPassword() {
        User user = user(UserStatus.ACTIVE);
        UserWithdrawalRequest request = request("WrongPassword1!", "reason");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> myPageService.withdraw(user, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void withdrawRejectsDisabledAccount() {
        User user = user(UserStatus.SUSPENDED);
        UserWithdrawalRequest request = request("Password1!", "reason");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> myPageService.withdraw(user, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_DISABLED);
    }

    private UserWithdrawalRequest request(String currentPassword, String reason) {
        UserWithdrawalRequest request = new UserWithdrawalRequest();
        ReflectionTestUtils.setField(request, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(request, "reason", reason);
        return request;
    }

    private User user(UserStatus status) {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .emailVerified(true)
                .build();
    }
}
