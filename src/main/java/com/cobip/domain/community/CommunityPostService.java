package com.cobip.domain.community;

import java.util.Locale;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.community.CommunityPostCreateRequest;
import com.cobip.dto.community.CommunityPostDetailResponse;
import com.cobip.dto.community.CommunityPostSummaryResponse;
import com.cobip.dto.community.CommunityPostUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import jakarta.persistence.criteria.Join;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final CommunityPostTextExtractor textExtractor;

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostSummaryResponse> getPosts(
        String keyword,
        CommunityPostCategory category,
        CommunityPostSort sort,
        Pageable pageable
    ) {
        CommunityPostSort sortType = sort == null ? CommunityPostSort.LATEST : sort;
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortType.toSort());
        Page<CommunityPostSummaryResponse> posts = communityPostRepository
                .findAll(postSpec(keyword, category), sortedPageable)
                .map(CommunityPostSummaryResponse::from);
        return PageResponse.from(posts);
    }

    @Transactional
    public CommunityPostDetailResponse getPost(Long postId) {
        CommunityPost post = findVisiblePost(postId);
        post.increaseViewCount();
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public CommunityPostDetailResponse createPost(User author, CommunityPostCreateRequest request) {
        validateContentJson(request.getContentJson());
        User managedAuthor = getManagedUser(author);
        String searchableText = textExtractor.extract(request.getContentJson());
        CommunityPost post = communityPostRepository.save(CommunityPost.create(managedAuthor, request, searchableText));
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public CommunityPostDetailResponse updatePost(User user, Long postId, CommunityPostUpdateRequest request) {
        CommunityPost post = findVisiblePost(postId);
        validateAuthor(post, user);

        String searchableText = null;
        if (request.getContentJson() != null) {
            validateContentJson(request.getContentJson());
            searchableText = textExtractor.extract(request.getContentJson());
        }

        post.update(request, searchableText);
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public void deletePost(User user, Long postId) {
        CommunityPost post = findVisiblePost(postId);
        validateAuthor(post, user);
        post.delete();
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

    private void validateAuthor(CommunityPost post, User user) {
        if (!post.isAuthor(user)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateContentJson(com.fasterxml.jackson.databind.JsonNode contentJson) {
        if (contentJson == null || contentJson.isNull() || !contentJson.isContainerNode()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Specification<CommunityPost> postSpec(String keyword, CommunityPostCategory category) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            var predicate = criteriaBuilder.and(
                    criteriaBuilder.isNull(root.get("deletedAt")),
                    criteriaBuilder.equal(root.get("status"), CommunityPostStatus.VISIBLE)
            );

            if (category != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                Join<CommunityPost, User> authorJoin = root.join("author");
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("searchableText")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(authorJoin.get("nickname")), likeKeyword)
                ));
            }
            return predicate;
        };
    }
}
