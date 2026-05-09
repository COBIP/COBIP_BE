package com.cobip.domain.lab;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabWorkspaceRepository extends JpaRepository<LabWorkspace, Long> {

    Optional<LabWorkspace> findByUserIdAndWorkspaceKey(Long userId, String workspaceKey);

    Page<LabWorkspace> findByUserIdOrderByLastOpenedAtDescIdDesc(Long userId, Pageable pageable);
}
