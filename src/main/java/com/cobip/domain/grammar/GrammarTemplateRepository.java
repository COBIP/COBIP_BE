package com.cobip.domain.grammar;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrammarTemplateRepository extends JpaRepository<GrammarTemplate, Long>, JpaSpecificationExecutor<GrammarTemplate> {

    Optional<GrammarTemplate> findByIdAndDeletedAtIsNull(Long id);

    Optional<GrammarTemplate> findByIdAndStatusAndDeletedAtIsNull(Long id, GrammarTemplateStatus status);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long id);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatus(GrammarTemplateStatus status);

    @Query("""
            select distinct gt.category
            from GrammarTemplate gt
            where gt.deletedAt is null
              and gt.status = :status
              and (:language is null or gt.language = :language)
            order by gt.category
            """)
    List<String> findDistinctCategories(
            @Param("status") GrammarTemplateStatus status,
            @Param("language") GrammarTemplateLanguage language
    );
}
