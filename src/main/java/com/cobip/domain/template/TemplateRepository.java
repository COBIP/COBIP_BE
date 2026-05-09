package com.cobip.domain.template;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndVisibility(TemplateVisibility visibility);

    long countByDeletedAtIsNullAndAccessLevel(TemplateAccessLevel accessLevel);

    @Query("""
            select distinct t.category
            from Template t
            where t.deletedAt is null
              and t.visibility = com.cobip.domain.template.TemplateVisibility.PUBLIC
            order by t.category
            """)
    List<String> findPublicCategories();

    @Query("""
            select distinct techStack
            from Template t
            join t.techStacks techStack
            where t.deletedAt is null
              and t.visibility = com.cobip.domain.template.TemplateVisibility.PUBLIC
            order by techStack
            """)
    List<String> findPublicTechStacks();
}
