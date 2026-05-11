package com.cobip.domain.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateTestCaseRepository extends JpaRepository<TemplateTestCase, Long> {

    List<TemplateTestCase> findByTemplateIdOrderByOrderIndexAscIdAsc(Long templateId);
}
