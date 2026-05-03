package com.cobip.domain.template;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateFavoriteRepository extends JpaRepository<TemplateFavorite, Long> {

    boolean existsByUserIdAndTemplateId(Long userId, Long templateId);

    Optional<TemplateFavorite> findByUserIdAndTemplateId(Long userId, Long templateId);

    @EntityGraph(attributePaths = {"template", "template.owner"})
    Page<TemplateFavorite> findByUserId(Long userId, Pageable pageable);
}
