package com.cobip.domain.community;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.community.CommunityPostSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityPostSaveService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostSaveRepository communityPostSaveRepository;
    private final UserRepository userRepository;

    @Transactional
    public void savePost(User user, Long postId) {
        User managedUser = getManagedUser(user);
        CommunityPost post = findVisiblePost(postId);
        if (communityPostSaveRepository.existsByUserIdAndPostId(managedUser.getId(), post.getId())) {
            throw new CustomException(ErrorCode.DUPLICATE_COMMUNITY_POST_SAVE);
        }
        communityPostSaveRepository.save(CommunityPostSave.create(managedUser, post));
    }

    @Transactional
    public void unsavePost(User user, Long postId) {
        User managedUser = getManagedUser(user);
        findVisiblePost(postId);
        communityPostSaveRepository.findByUserIdAndPostId(managedUser.getId(), postId)
                .ifPresent(communityPostSaveRepository::delete);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostSummaryResponse> getSavedPosts(User user, Pageable pageable) {
        return PageResponse.from(communityPostSaveRepository
                .findVisibleSavedPosts(user.getId(), pageable)
                .map(CommunityPostSave::getPost)
                .map(CommunityPostSummaryResponse::from));
    }

    private CommunityPost findVisiblePost(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!post.isVisible()) {
            throw new CustomException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        return post;
    }

    private User getManagedUser(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
