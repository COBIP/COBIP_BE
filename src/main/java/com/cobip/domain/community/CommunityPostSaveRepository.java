package com.cobip.domain.community;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostSaveRepository extends JpaRepository<CommunityPostSave, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<CommunityPostSave> findByUserIdAndPostId(Long userId, Long postId);

    @EntityGraph(attributePaths = {"post", "post.author"})
    @Query("""
            select s
            from CommunityPostSave s
            join s.post p
            where s.user.id = :userId
              and p.deletedAt is null
              and p.status = com.cobip.domain.community.CommunityPostStatus.VISIBLE
            order by s.createdAt desc, s.id desc
            """)
    Page<CommunityPostSave> findVisibleSavedPosts(@Param("userId") Long userId, Pageable pageable);
}
