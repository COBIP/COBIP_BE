package com.cobip.domain.community;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityPostCreateRequest;
import com.cobip.dto.community.CommunityPostUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "community_posts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommunityPostCategory category;

    @Column(nullable = false, length = 120)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentJson;

    @Lob
    @Column(nullable = false)
    private String searchableText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityPostStatus status;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private long commentCount;

    @Column(nullable = false)
    private long likeCount;

    private LocalDateTime deletedAt;

    public static CommunityPost create(
        User author,
        CommunityPostCreateRequest request,
        String searchableText
    ) {
        return CommunityPost.builder()
                .author(author)
                .category(request.getCategory())
                .title(request.getTitle())
                .contentJson(request.getContentJson())
                .searchableText(searchableText)
                .status(CommunityPostStatus.VISIBLE)
                .viewCount(0)
                .commentCount(0)
                .likeCount(0)
                .build();
    }

    public void update(CommunityPostUpdateRequest request, String searchableText) {
        if (request.getCategory() != null) {
            this.category = request.getCategory();
        }
        if (request.getTitle() != null) {
            this.title = request.getTitle();
        }
        if (request.getContentJson() != null) {
            this.contentJson = request.getContentJson();
            this.searchableText = searchableText;
        }
    }

    public boolean isAuthor(User user) {
        return user != null && author.getId().equals(user.getId());
    }

    public boolean isVisible() {
        return status == CommunityPostStatus.VISIBLE && deletedAt == null;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
