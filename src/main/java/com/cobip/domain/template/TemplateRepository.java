package com.cobip.domain.template;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TemplateRepository extends JpaRepository<Template, Long>, JpaSpecificationExecutor<Template> {

    @EntityGraph(attributePaths = {"owner"})
    Optional<Template> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"owner"})
    Page<Template> findByOwnerIdAndDeletedAtIsNull(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<Template> findByDeletedAtIsNullAndVisibilityOrderByFavoriteCountDescViewCountDesc(
            TemplateVisibility visibility,
            Pageable pageable
    );

    long countByOwnerIdAndDeletedAtIsNull(Long ownerId);
}
