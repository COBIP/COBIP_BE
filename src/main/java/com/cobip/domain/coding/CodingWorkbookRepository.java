package com.cobip.domain.coding;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodingWorkbookRepository extends JpaRepository<CodingWorkbook, Long>, JpaSpecificationExecutor<CodingWorkbook> {

    Optional<CodingWorkbook> findByIdAndStatusAndDeletedAtIsNull(Long id, CodingWorkbookStatus status);
}
