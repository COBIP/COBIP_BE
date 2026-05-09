package com.cobip.dto.community;

import java.time.LocalDateTime;

import com.cobip.domain.community.CommunityComment;

import lombok.Getter;

@Getter
public class CommunityCommentResponse {

    private final Long id;
    private final Long postId;
    private final Long parentCommentId;
    private final Long authorId;
    private final String authorNickname;
    private final String content;
    private final long likeCount;
    private final long replyCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CommunityCommentResponse(CommunityComment comment, long replyCount) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId();
        this.parentCommentId = comment.getParentComment() == null ? null : comment.getParentComment().getId();
        this.authorId = comment.getAuthor().getId();
        this.authorNickname = comment.getAuthor().getNickname();
        this.content = comment.getContent();
        this.likeCount = comment.getLikeCount();
        this.replyCount = replyCount;
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

    public static CommunityCommentResponse of(CommunityComment comment, long replyCount) {
        return new CommunityCommentResponse(comment, replyCount);
    }
}
