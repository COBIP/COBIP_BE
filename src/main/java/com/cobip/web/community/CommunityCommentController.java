package com.cobip.web.community;

import com.cobip.domain.community.CommunityCommentService;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityCommentCreateRequest;
import com.cobip.dto.community.CommunityCommentResponse;
import com.cobip.dto.community.CommunityCommentUpdateRequest;
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
@RequestMapping("/api/v1/community")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommunityCommentResponse>>> getComments(
        @PathVariable Long postId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityCommentService.getComments(postId, pageable)));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> createComment(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId,
        @RequestBody @Valid CommunityCommentCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("댓글이 작성되었습니다.", communityCommentService.createComment(user, postId, request)));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<PageResponse<CommunityCommentResponse>>> getReplies(
        @PathVariable Long commentId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityCommentService.getReplies(commentId, pageable)));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> updateComment(
        @AuthenticationPrincipal User user,
        @PathVariable Long commentId,
        @RequestBody @Valid CommunityCommentUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", communityCommentService.updateComment(user, commentId, request)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
        @AuthenticationPrincipal User user,
        @PathVariable Long commentId
    ) {
        communityCommentService.deleteComment(user, commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }
}
