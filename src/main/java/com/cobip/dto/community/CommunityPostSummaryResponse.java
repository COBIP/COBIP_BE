package com.cobip.dto.community;

import java.time.LocalDateTime;

import com.cobip.domain.community.CommunityPost;
import com.cobip.domain.community.CommunityPostCategory;

import lombok.Getter;

@Getter
public class CommunityPostSummaryResponse {

    private final Long id;
    private final CommunityPostCategory category;
    private final String title;
    private final Long authorId;
    private final String authorNickname;
    private final long viewCount;
    private final long commentCount;
    private final long likeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    protected CommunityPostSummaryResponse(CommunityPost post) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.authorId = post.getAuthor().getId();
        this.authorNickname = post.getAuthor().getNickname();
        this.viewCount = post.getViewCount();
        this.commentCount = post.getCommentCount();
        this.likeCount = post.getLikeCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

    public static CommunityPostSummaryResponse from(CommunityPost post) {
        return new CommunityPostSummaryResponse(post);
    }
}
