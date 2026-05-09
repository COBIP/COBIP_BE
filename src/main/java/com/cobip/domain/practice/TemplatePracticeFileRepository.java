package com.cobip.domain.practice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplatePracticeFileRepository extends JpaRepository<TemplatePracticeFile, Long> {

    List<TemplatePracticeFile> findByTemplateIdOrderByOrderIndexAscIdAsc(Long templateId);

    Optional<TemplatePracticeFile> findByIdAndTemplateId(Long id, Long templateId);
}
