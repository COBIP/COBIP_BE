package com.cobip.web.community;

import com.cobip.domain.community.CommunityPostSaveService;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityPostSummaryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/posts")
public class CommunityPostSaveController {

    private final CommunityPostSaveService communityPostSaveService;

    @PostMapping("/{postId}/save")
    public ResponseEntity<ApiResponse<Void>> savePost(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId
    ) {
        communityPostSaveService.savePost(user, postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{postId}/save")
    public ResponseEntity<ApiResponse<Void>> unsavePost(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId
    ) {
        communityPostSaveService.unsavePost(user, postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<PageResponse<CommunityPostSummaryResponse>>> getSavedPosts(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityPostSaveService.getSavedPosts(user, pageable)));
    }
}
