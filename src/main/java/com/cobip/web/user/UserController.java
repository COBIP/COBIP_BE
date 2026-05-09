package com.cobip.web.user;

import com.cobip.domain.certificate.CertificateService;
import com.cobip.domain.user.MyPageService;
import com.cobip.domain.user.User;
import com.cobip.dto.mypage.ActivityHistoryResponse;
import com.cobip.dto.mypage.CertificateResponse;
import com.cobip.dto.mypage.LearningProgressResponse;
import com.cobip.dto.mypage.MyDashboardResponse;
import com.cobip.dto.mypage.SubscriptionResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.user.MyProfileResponse;
import com.cobip.dto.user.MyProfileUpdateRequest;
import com.cobip.dto.user.PasswordChangeRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final MyPageService myPageService;
    private final CertificateService certificateService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyProfileResponse>> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getProfile(user)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<MyProfileResponse>> updateProfile(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid MyProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("내 정보가 수정되었습니다.", myPageService.updateProfile(user, request)));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid PasswordChangeRequest request
    ) {
        myPageService.changePassword(user, request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getMyTemplates(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getMyTemplates(user, pageable)));
    }

    @GetMapping("/favorite-templates")
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getFavoriteTemplates(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getFavoriteTemplates(user, pageable)));
    }

    @GetMapping("/learning")
    public ResponseEntity<ApiResponse<PageResponse<LearningProgressResponse>>> getLearningProgress(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getLearningProgress(user, pageable)));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<PageResponse<ActivityHistoryResponse>>> getActivities(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getActivities(user, pageable)));
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getSubscription(user)));
    }

    @PatchMapping("/subscription/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("구독 해지가 신청되었습니다.", myPageService.cancelSubscription(user)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyDashboardResponse>> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(myPageService.getDashboard(user)));
    }

    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<PageResponse<CertificateResponse>>> getCertificates(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getMyCertificates(user, pageable)));
    }
}
