package com.cobip.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.Subscription;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.subscription.SubscriptionStatus;
import com.cobip.domain.template.TemplateFavoriteRepository;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.dto.mypage.SubscriptionResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFavoriteRepository templateFavoriteRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

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
                activityHistoryRepository,
                subscriptionRepository,
                passwordEncoder
        );
    }

    @Test
    void cancelSubscriptionMarksSubscriptionAsCanceled() {
        User user = user();
        Subscription subscription = subscription(SubscriptionStatus.ACTIVE, LocalDate.now().plusDays(10));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

        SubscriptionResponse response = myPageService.cancelSubscription(user);

        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(subscription.getNextPaymentAt()).isNull();
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void cancelSubscriptionRejectsMissingSubscription() {
        User user = user();

        assertThatThrownBy(() -> myPageService.cancelSubscription(user))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SUBSCRIPTION_REQUIRED);
    }

    @Test
    void cancelSubscriptionRejectsExpiredSubscription() {
        User user = user();
        Subscription subscription = subscription(SubscriptionStatus.ACTIVE, LocalDate.now().minusDays(1));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> myPageService.cancelSubscription(user))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
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

    private Subscription subscription(SubscriptionStatus status, LocalDate expiredAt) {
        return Subscription.builder()
                .id(1L)
                .user(user())
                .planName("Pro")
                .status(status)
                .startedAt(LocalDate.now().minusDays(20))
                .expiredAt(expiredAt)
                .nextPaymentAt(expiredAt)
                .build();
    }
}
