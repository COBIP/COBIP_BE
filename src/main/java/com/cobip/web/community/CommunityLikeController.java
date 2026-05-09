package com.cobip.web.community;

import com.cobip.domain.community.CommunityLikeService;
import com.cobip.domain.user.User;
import com.cobip.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
public class CommunityLikeController {

    private final CommunityLikeService communityLikeService;

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> addPostLike(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId
    ) {
        communityLikeService.addPostLike(user, postId);
        return ResponseEntity.ok(ApiResponse.success("게시글에 좋아요를 추가했습니다.", null));
    }

    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> removePostLike(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId
    ) {
        communityLikeService.removePostLike(user, postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요를 취소했습니다.", null));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> addCommentLike(
        @AuthenticationPrincipal User user,
        @PathVariable Long commentId
    ) {
        communityLikeService.addCommentLike(user, commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글에 좋아요를 추가했습니다.", null));
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> removeCommentLike(
        @AuthenticationPrincipal User user,
        @PathVariable Long commentId
    ) {
        communityLikeService.removeCommentLike(user, commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글 좋아요를 취소했습니다.", null));
    }
}
