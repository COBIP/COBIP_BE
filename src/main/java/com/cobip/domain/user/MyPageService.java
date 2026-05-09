package com.cobip.domain.user;

import java.util.List;

import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.subscription.Subscription;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.template.TemplateFavoriteRepository;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.dto.mypage.ActivityHistoryResponse;
import com.cobip.dto.mypage.LearningProgressResponse;
import com.cobip.dto.mypage.MyDashboardResponse;
import com.cobip.dto.mypage.SubscriptionResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.user.MyProfileResponse;
import com.cobip.dto.user.MyProfileUpdateRequest;
import com.cobip.dto.user.PasswordChangeRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final TemplateFavoriteRepository templateFavoriteRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final ActivityHistoryRepository activityHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyProfileResponse getProfile(User user) {
        return MyProfileResponse.from(user);
    }

    @Transactional
    public MyProfileResponse updateProfile(User user, MyProfileUpdateRequest request) {
        User managedUser = getManagedUser(user);
        if (request.getNickname() != null
                && !request.getNickname().isBlank()
                && userRepository.existsByNicknameAndIdNot(request.getNickname(), managedUser.getId())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        managedUser.updateProfile(request.getNickname(), request.getProfileImageUrl());
        return MyProfileResponse.from(managedUser);
    }

    @Transactional
    public void changePassword(User user, PasswordChangeRequest request) {
        User managedUser = getManagedUser(user);
        if (!passwordEncoder.matches(request.getCurrentPassword(), managedUser.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        managedUser.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryResponse> getMyTemplates(User user, Pageable pageable) {
        Page<TemplateSummaryResponse> templates = templateRepository.findByOwnerIdAndDeletedAtIsNull(user.getId(), pageable)
                .map(TemplateSummaryResponse::from);
        return PageResponse.from(templates);
    }

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryResponse> getFavoriteTemplates(User user, Pageable pageable) {
        Page<TemplateSummaryResponse> templates = templateFavoriteRepository.findByUserId(user.getId(), pageable)
                .map(favorite -> TemplateSummaryResponse.from(favorite.getTemplate()));
        return PageResponse.from(templates);
    }

    @Transactional(readOnly = true)
    public PageResponse<LearningProgressResponse> getLearningProgress(User user, Pageable pageable) {
        Page<LearningProgressResponse> progresses = learningProgressRepository
                .findByUserIdOrderByLastAccessedAtDesc(user.getId(), pageable)
                .map(LearningProgressResponse::from);
        return PageResponse.from(progresses);
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityHistoryResponse> getActivities(User user, Pageable pageable) {
        Page<ActivityHistoryResponse> activities = activityHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(ActivityHistoryResponse::from);
        return PageResponse.from(activities);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(User user) {
        return subscriptionRepository.findByUserId(user.getId())
                .map(SubscriptionResponse::from)
                .orElseGet(SubscriptionResponse::none);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(User user) {
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_REQUIRED));

        if (!subscription.canCancel()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        subscription.cancelRenewal();
        return SubscriptionResponse.from(subscription);
    }

    @Transactional(readOnly = true)
    public MyDashboardResponse getDashboard(User user) {
        List<LearningProgress> progresses = learningProgressRepository.findByUserId(user.getId());
        long totalStudySeconds = progresses.stream().mapToLong(LearningProgress::getStudySeconds).sum();
        int solvedCount = progresses.stream().mapToInt(LearningProgress::getSolvedCount).sum();
        int correctCount = progresses.stream().mapToInt(LearningProgress::getCorrectCount).sum();
        double averageCorrectRate = solvedCount == 0 ? 0 : (double) correctCount / solvedCount;

        List<LearningProgressResponse> recentLearning = learningProgressRepository
                .findTop5ByUserIdOrderByLastAccessedAtDesc(user.getId())
                .stream()
                .map(LearningProgressResponse::from)
                .toList();
        List<ActivityHistoryResponse> recentActivities = activityHistoryRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ActivityHistoryResponse::from)
                .toList();
        List<TemplateSummaryResponse> popularTemplates = templateRepository
                .findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
                        TemplateVisibility.PUBLIC,
                        PageRequest.of(0, 5)
                )
                .stream()
                .map(TemplateSummaryResponse::from)
                .toList();

        return new MyDashboardResponse(
                templateRepository.countByOwnerIdAndDeletedAtIsNull(user.getId()),
                learningProgressRepository.countByUserIdAndProgressPercentLessThan(user.getId(), 100),
                learningProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(user.getId(), 100),
                totalStudySeconds,
                averageCorrectRate,
                getSubscription(user),
                popularTemplates,
                recentLearning,
                recentActivities
        );
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
