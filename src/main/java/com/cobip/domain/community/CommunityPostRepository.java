package com.cobip.domain.community;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityPostRepository
        extends JpaRepository<CommunityPost, Long>, JpaSpecificationExecutor<CommunityPost> {

    @EntityGraph(attributePaths = {"author"})
    Optional<CommunityPost> findByIdAndDeletedAtIsNull(Long id);
}
