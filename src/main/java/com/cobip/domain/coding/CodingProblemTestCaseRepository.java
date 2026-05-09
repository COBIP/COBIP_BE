package com.cobip.domain.coding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingProblemTestCaseRepository extends JpaRepository<CodingProblemTestCase, Long> {

    List<CodingProblemTestCase> findByProblemIdAndSampleOrderByOrderIndexAsc(Long problemId, boolean sample);
}
