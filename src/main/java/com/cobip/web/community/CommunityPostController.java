package com.cobip.web.community;

import com.cobip.domain.community.CommunityPostCategory;
import com.cobip.domain.community.CommunityPostService;
import com.cobip.domain.community.CommunityPostSort;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityPostCreateRequest;
import com.cobip.dto.community.CommunityPostDetailResponse;
import com.cobip.dto.community.CommunityPostSummaryResponse;
import com.cobip.dto.community.CommunityPostUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/posts")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CommunityPostSummaryResponse>>> getPosts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) CommunityPostCategory category,
        @RequestParam(required = false) CommunityPostSort sort,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityPostService.getPosts(keyword, category, sort, pageable)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(communityPostService.getPost(postId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> createPost(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid CommunityPostCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다.", communityPostService.createPost(user, request)));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostDetailResponse>> updatePost(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId,
        @RequestBody @Valid CommunityPostUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", communityPostService.updatePost(user, postId, request)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
        @AuthenticationPrincipal User user,
        @PathVariable Long postId
    ) {
        communityPostService.deletePost(user, postId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }
}
