package com.cobip.domain.certificate;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    boolean existsByUserIdAndTemplateId(Long userId, Long templateId);

    @EntityGraph(attributePaths = {"template", "template.owner"})
    Optional<Certificate> findByUserIdAndTemplateId(Long userId, Long templateId);

    @EntityGraph(attributePaths = {"template", "template.owner"})
    Page<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId, Pageable pageable);
}
