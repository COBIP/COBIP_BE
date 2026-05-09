package com.cobip.domain.community;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.community.CommunityCommentCreateRequest;
import com.cobip.dto.community.CommunityCommentResponse;
import com.cobip.dto.community.CommunityCommentUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommunityCommentResponse> getComments(Long postId, Pageable pageable) {
        CommunityPost post = findVisiblePost(postId);
        Page<CommunityCommentResponse> comments = communityCommentRepository
                .findByPostIdAndParentCommentIsNullAndDeletedAtIsNullAndStatusOrderByCreatedAtAsc(
                        post.getId(),
                        CommunityCommentStatus.VISIBLE,
                        pageable
                )
                .map(this::toResponse);
        return PageResponse.from(comments);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityCommentResponse> getReplies(Long commentId, Pageable pageable) {
        CommunityComment parentComment = findVisibleComment(commentId);
        Page<CommunityCommentResponse> comments = communityCommentRepository
                .findByParentCommentIdAndDeletedAtIsNullAndStatusOrderByCreatedAtAsc(
                        parentComment.getId(),
                        CommunityCommentStatus.VISIBLE,
                        pageable
                )
                .map(comment -> CommunityCommentResponse.of(comment, 0));
        return PageResponse.from(comments);
    }

    @Transactional
    public CommunityCommentResponse createComment(
        User author,
        Long postId,
        CommunityCommentCreateRequest request
    ) {
        CommunityPost post = findVisiblePost(postId);
        User managedAuthor = getManagedUser(author);
        CommunityComment parentComment = findParentComment(post, request.getParentCommentId());
        CommunityComment comment = communityCommentRepository.save(
                CommunityComment.create(post, managedAuthor, parentComment, request.getContent().trim())
        );
        post.increaseCommentCount();
        return CommunityCommentResponse.of(comment, 0);
    }

    @Transactional
    public CommunityCommentResponse updateComment(
        User user,
        Long commentId,
        CommunityCommentUpdateRequest request
    ) {
        CommunityComment comment = findVisibleComment(commentId);
        validateAuthor(comment, user);
        comment.updateContent(request.getContent().trim());
        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(User user, Long commentId) {
        CommunityComment comment = findVisibleComment(commentId);
        validateAuthor(comment, user);
        comment.delete();
        comment.getPost().decreaseCommentCount();
    }

    private CommunityCommentResponse toResponse(CommunityComment comment) {
        long replyCount = comment.isReply()
                ? 0
                : communityCommentRepository.countByParentCommentIdAndDeletedAtIsNull(comment.getId());
        return CommunityCommentResponse.of(comment, replyCount);
    }

    private CommunityPost findVisiblePost(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (!post.isVisible()) {
            throw new CustomException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        return post;
    }

    private CommunityComment findVisibleComment(Long commentId) {
        CommunityComment comment = communityCommentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));

        if (!comment.isVisible()) {
            throw new CustomException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private CommunityComment findParentComment(CommunityPost post, Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        CommunityComment parentComment = findVisibleComment(parentCommentId);
        if (!parentComment.getPost().getId().equals(post.getId()) || parentComment.isReply()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return parentComment;
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateAuthor(CommunityComment comment, User user) {
        if (!comment.isAuthor(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
