package com.cobip.domain.coding;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long>, JpaSpecificationExecutor<CodingProblem> {

    Optional<CodingProblem> findByIdAndPublishedTrueAndDeletedAtIsNull(Long id);
}
