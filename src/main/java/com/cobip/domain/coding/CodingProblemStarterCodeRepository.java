package com.cobip.domain.coding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingProblemStarterCodeRepository extends JpaRepository<CodingProblemStarterCode, Long> {

    List<CodingProblemStarterCode> findByProblemIdOrderByLanguageAsc(Long problemId);
}
