package com.cobip.domain.grammar;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GrammarTemplateRepository extends JpaRepository<GrammarTemplate, Long>, JpaSpecificationExecutor<GrammarTemplate> {

    Optional<GrammarTemplate> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long id);
}
