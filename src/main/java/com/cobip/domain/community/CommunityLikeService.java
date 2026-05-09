package com.cobip.domain.community;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityLikeService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addPostLike(User user, Long postId) {
        CommunityPost post = findVisiblePost(postId);
        User managedUser = getManagedUser(user);
        addLike(managedUser, CommunityLikeTargetType.POST, post.getId());
        post.increaseLikeCount();
    }

    @Transactional
    public void removePostLike(User user, Long postId) {
        CommunityPost post = findVisiblePost(postId);
        User managedUser = getManagedUser(user);
        removeLike(managedUser, CommunityLikeTargetType.POST, post.getId(), post::decreaseLikeCount);
    }

    @Transactional
    public void addCommentLike(User user, Long commentId) {
        CommunityComment comment = findVisibleComment(commentId);
        User managedUser = getManagedUser(user);
        addLike(managedUser, CommunityLikeTargetType.COMMENT, comment.getId());
        comment.increaseLikeCount();
    }

    @Transactional
    public void removeCommentLike(User user, Long commentId) {
        CommunityComment comment = findVisibleComment(commentId);
        User managedUser = getManagedUser(user);
        removeLike(managedUser, CommunityLikeTargetType.COMMENT, comment.getId(), comment::decreaseLikeCount);
    }

    private void addLike(User user, CommunityLikeTargetType targetType, Long targetId) {
        if (communityLikeRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId)) {
            throw new CustomException(ErrorCode.DUPLICATE_COMMUNITY_LIKE);
        }
        communityLikeRepository.save(CommunityLike.create(user, targetType, targetId));
    }

    private void removeLike(
        User user,
        CommunityLikeTargetType targetType,
        Long targetId,
        Runnable afterDelete
    ) {
        communityLikeRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId)
                .ifPresent(like -> {
                    communityLikeRepository.delete(like);
                    afterDelete.run();
                });
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

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
