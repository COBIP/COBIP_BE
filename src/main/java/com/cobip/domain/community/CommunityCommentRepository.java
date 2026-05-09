package com.cobip.domain.community;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    @EntityGraph(attributePaths = {"author", "post"})
    Optional<CommunityComment> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"author", "post"})
    Page<CommunityComment> findByPostIdAndParentCommentIsNullAndDeletedAtIsNullAndStatusOrderByCreatedAtAsc(
            Long postId,
            CommunityCommentStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "post", "parentComment"})
    Page<CommunityComment> findByParentCommentIdAndDeletedAtIsNullAndStatusOrderByCreatedAtAsc(
            Long parentCommentId,
            CommunityCommentStatus status,
            Pageable pageable
    );

    long countByParentCommentIdAndDeletedAtIsNull(Long parentCommentId);
}
