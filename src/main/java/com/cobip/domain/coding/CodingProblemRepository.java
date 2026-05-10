package com.cobip.domain.coding;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long>, JpaSpecificationExecutor<CodingProblem> {

    Optional<CodingProblem> findByIdAndStatusAndDeletedAtIsNull(Long id, CodingProblemStatus status);

    Optional<CodingProblem> findByIdAndWorkbookIdAndDeletedAtIsNull(Long id, Long workbookId);

    List<CodingProblem> findByWorkbookIdAndStatusAndDeletedAtIsNullOrderByOrderIndexAsc(
            Long workbookId,
            CodingProblemStatus status
    );

    List<CodingProblem> findByWorkbookIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(Long workbookId);
}
