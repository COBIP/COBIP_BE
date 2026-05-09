package com.cobip.domain.community;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    boolean existsByUserIdAndTargetTypeAndTargetId(
            Long userId,
            CommunityLikeTargetType targetType,
            Long targetId
    );

    Optional<CommunityLike> findByUserIdAndTargetTypeAndTargetId(
            Long userId,
            CommunityLikeTargetType targetType,
            Long targetId
    );
}
