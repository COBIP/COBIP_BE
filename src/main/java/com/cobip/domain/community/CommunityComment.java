package com.cobip.domain.community;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_comments")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommunityComment parentComment;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityCommentStatus status;

    @Column(nullable = false)
    private long likeCount;

    private LocalDateTime deletedAt;

    public static CommunityComment create(
        CommunityPost post,
        User author,
        CommunityComment parentComment,
        String content
    ) {
        return CommunityComment.builder()
                .post(post)
                .author(author)
                .parentComment(parentComment)
                .content(content)
                .status(CommunityCommentStatus.VISIBLE)
                .likeCount(0)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean isAuthor(User user) {
        return user != null && author.getId().equals(user.getId());
    }

    public boolean isReply() {
        return parentComment != null;
    }

    public boolean isVisible() {
        return status == CommunityCommentStatus.VISIBLE && deletedAt == null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
