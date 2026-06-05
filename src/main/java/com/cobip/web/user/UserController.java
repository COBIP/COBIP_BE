package com.cobip.web.user;

import com.cobip.domain.certificate.CertificateService;
import com.cobip.domain.learning.AiTemplateService;
import com.cobip.domain.user.MyPageService;
import com.cobip.domain.user.User;
import com.cobip.dto.mypage.ActivityHistoryResponse;
import com.cobip.dto.mypage.AiTemplateResponse;
import com.cobip.dto.mypage.AiTemplateSaveRequest;
import com.cobip.dto.mypage.AiTemplateUpdateRequest;
import com.cobip.dto.mypage.CertificateResponse;
import com.cobip.dto.mypage.LearningActivityHeartbeatRequest;
import com.cobip.dto.mypage.LearningActivityHeartbeatResponse;
import com.cobip.dto.mypage.LearningProgressResponse;
import com.cobip.dto.mypage.MyDashboardResponse;
import com.cobip.dto.mypage.SubscriptionResponse;
import com.cobip.dto.template.TemplateSummaryResponse;
import com.cobip.dto.user.MyProfileResponse;
import com.cobip.dto.user.MyProfileUpdateRequest;
import com.cobip.dto.user.PasswordChangeRequest;
import com.cobip.dto.user.UserWithdrawalRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final MyPageService myPageService;
    private final CertificateService certificateService;
    private final AiTemplateService aiTemplateService;

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

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid UserWithdrawalRequest request
    ) {
        myPageService.withdraw(user, request);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
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

    @PostMapping("/ai-templates")
    public ResponseEntity<ApiResponse<AiTemplateResponse>> saveAiTemplate(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid AiTemplateSaveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI 템플릿이 저장되었습니다.",
                aiTemplateService.saveTemplate(user, request)
        ));
    }

    @GetMapping("/ai-templates")
    public ResponseEntity<ApiResponse<PageResponse<AiTemplateResponse>>> getAiTemplates(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(aiTemplateService.getTemplates(user, pageable)));
    }

    @GetMapping("/ai-templates/{aiTemplateId}")
    public ResponseEntity<ApiResponse<AiTemplateResponse>> getAiTemplate(
        @AuthenticationPrincipal User user,
        @PathVariable String aiTemplateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(aiTemplateService.getTemplate(user, aiTemplateId)));
    }

    @PatchMapping("/ai-templates/{aiTemplateId}")
    public ResponseEntity<ApiResponse<AiTemplateResponse>> updateAiTemplate(
        @AuthenticationPrincipal User user,
        @PathVariable String aiTemplateId,
        @RequestBody @Valid AiTemplateUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI 템플릿이 수정되었습니다.",
                aiTemplateService.updateTemplate(user, aiTemplateId, request)
        ));
    }

    @DeleteMapping("/ai-templates/{aiTemplateId}")
    public ResponseEntity<ApiResponse<Void>> deleteAiTemplate(
        @AuthenticationPrincipal User user,
        @PathVariable String aiTemplateId
    ) {
        aiTemplateService.deleteTemplate(user, aiTemplateId);
        return ResponseEntity.ok(ApiResponse.success("AI 템플릿이 삭제되었습니다.", null));
    }

    @PostMapping("/learning-activities/heartbeat")
    public ResponseEntity<ApiResponse<LearningActivityHeartbeatResponse>> recordLearningActivityHeartbeat(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid LearningActivityHeartbeatRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Learning activity recorded.",
                myPageService.recordLearningActivityHeartbeat(user, request)
        ));
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
