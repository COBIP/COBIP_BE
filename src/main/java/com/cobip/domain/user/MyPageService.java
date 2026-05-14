package com.cobip.domain.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.cobip.domain.activity.ActivityHistory;
import com.cobip.domain.activity.ActivityHistoryRepository;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.learning.GrammarLearningProgressRepository;
import com.cobip.domain.learning.GrammarLearningProgressService;
import com.cobip.domain.learning.LearningContentType;
import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.learning.UserLearningDailyStat;
import com.cobip.domain.learning.UserLearningDailyStatRepository;
import com.cobip.domain.subscription.Subscription;
import com.cobip.domain.subscription.SubscriptionRepository;
import com.cobip.domain.template.TemplateFavoriteRepository;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.dto.mypage.ActivityHistoryResponse;
import com.cobip.dto.mypage.LearningActivityHeartbeatRequest;
import com.cobip.dto.mypage.LearningActivityHeartbeatResponse;
import com.cobip.dto.mypage.LearningProgressResponse;
import com.cobip.dto.mypage.MyDashboardResponse;
import com.cobip.dto.mypage.SubscriptionResponse;
import com.cobip.dto.mypage.WeeklyActivityResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.user.MyProfileResponse;
import com.cobip.dto.user.MyProfileUpdateRequest;
import com.cobip.dto.user.PasswordChangeRequest;
import com.cobip.dto.user.UserWithdrawalRequest;
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
    private final GrammarLearningProgressRepository grammarLearningProgressRepository;
    private final GrammarLearningProgressService grammarLearningProgressService;
    private final UserLearningDailyStatRepository userLearningDailyStatRepository;
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

    @Transactional
    public void withdraw(User user, UserWithdrawalRequest request) {
        User managedUser = getManagedUser(user);
        if (!managedUser.isActiveAccount()) {
            throw new CustomException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), managedUser.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        managedUser.changeStatus(UserStatus.DELETED);
        activityHistoryRepository.save(ActivityHistory.builder()
                .user(managedUser)
                .type(ActivityType.USER_WITHDRAWN)
                .message("회원 탈퇴: " + request.getReason().trim())
                .targetType("USER")
                .targetId(managedUser.getId())
                .build());
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

    @Transactional
    public LearningActivityHeartbeatResponse recordLearningActivityHeartbeat(
        User user,
        LearningActivityHeartbeatRequest request
    ) {
        User managedUser = getManagedUser(user);
        LocalDate activityDate = LocalDate.now();
        long activeSeconds = request.getActiveSeconds();

        if (request.getContentTypeOrDefault() == LearningContentType.GRAMMAR_TEMPLATE) {
            grammarLearningProgressService.recordAccess(
                    managedUser,
                    request.getTemplateId(),
                    request.getChapterId(),
                    activeSeconds
            );
        }
        userLearningDailyStatRepository.addStudySeconds(managedUser.getId(), activityDate, activeSeconds);
        long studySeconds = userLearningDailyStatRepository
                .findByUserIdAndActivityDate(managedUser.getId(), activityDate)
                .map(UserLearningDailyStat::getStudySeconds)
                .orElse(activeSeconds);

        return new LearningActivityHeartbeatResponse(activityDate, studySeconds);
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
        long totalStudySeconds = progresses.stream().mapToLong(LearningProgress::getStudySeconds).sum()
                + userLearningDailyStatRepository.sumStudySecondsByUserId(user.getId());
        int solvedCount = progresses.stream().mapToInt(LearningProgress::getSolvedCount).sum();
        int correctCount = progresses.stream().mapToInt(LearningProgress::getCorrectCount).sum();
        double averageCorrectRate = solvedCount == 0 ? 0 : (double) correctCount / solvedCount;

        List<LearningProgressResponse> recentLearning = learningProgressRepository
                .findTop5ByUserIdOrderByLastAccessedAtDesc(user.getId()).stream()
                .map(LearningProgressResponse::from)
                .toList();
        List<LearningProgressResponse> recentGrammarLearning = grammarLearningProgressRepository
                .findTop5ByUserIdOrderByLastAccessedAtDesc(user.getId()).stream()
                .map(LearningProgressResponse::from)
                .toList();
        List<LearningProgressResponse> mergedRecentLearning = mergeRecentLearning(recentLearning, recentGrammarLearning);
        List<ActivityHistoryResponse> recentActivities = activityHistoryRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ActivityHistoryResponse::from)
                .toList();
        List<WeeklyActivityResponse> weeklyActivities = getWeeklyActivities(user);
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
                learningProgressRepository.countByUserIdAndProgressPercentLessThan(user.getId(), 100)
                        + grammarLearningProgressRepository.countByUserIdAndProgressPercentLessThan(user.getId(), 100),
                learningProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(user.getId(), 100)
                        + grammarLearningProgressRepository.countByUserIdAndProgressPercentGreaterThanEqual(user.getId(), 100),
                totalStudySeconds,
                averageCorrectRate,
                getSubscription(user),
                mergedRecentLearning.isEmpty() ? null : mergedRecentLearning.getFirst(),
                weeklyActivities,
                popularTemplates,
                mergedRecentLearning,
                recentActivities
        );
    }

    private List<LearningProgressResponse> mergeRecentLearning(
        List<LearningProgressResponse> templateLearning,
        List<LearningProgressResponse> grammarLearning
    ) {
        return Stream.concat(templateLearning.stream(), grammarLearning.stream())
                .sorted((left, right) -> lastAccessedAt(right).compareTo(lastAccessedAt(left)))
                .limit(5)
                .toList();
    }

    private LocalDateTime lastAccessedAt(LearningProgressResponse progress) {
        return progress.getLastAccessedAt() == null ? LocalDateTime.MIN : progress.getLastAccessedAt();
    }

    private List<WeeklyActivityResponse> getWeeklyActivities(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<ActivityHistory> histories = activityHistoryRepository
                .findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                        user.getId(),
                        startDate.atStartOfDay(),
                        today.plusDays(1).atStartOfDay()
                );
        List<UserLearningDailyStat> dailyStats = userLearningDailyStatRepository
                .findByUserIdAndActivityDateBetween(user.getId(), startDate, today);
        Map<LocalDate, Long> countsByDate = histories.stream()
                .collect(Collectors.groupingBy(
                        history -> history.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));
        Map<LocalDate, Long> studySecondsByDate = dailyStats.stream()
                .collect(Collectors.groupingBy(
                        UserLearningDailyStat::getActivityDate,
                        Collectors.summingLong(UserLearningDailyStat::getStudySeconds)
                ));

        return LongStream.rangeClosed(0, 6)
                .mapToObj(startDate::plusDays)
                .map(date -> new WeeklyActivityResponse(
                        date,
                        countsByDate.getOrDefault(date, 0L),
                        studySecondsByDate.getOrDefault(date, 0L)
                ))
                .toList();
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
