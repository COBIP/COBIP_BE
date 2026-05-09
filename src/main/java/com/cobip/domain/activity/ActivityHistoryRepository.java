package com.cobip.domain.activity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityHistoryRepository extends JpaRepository<ActivityHistory, Long>, JpaSpecificationExecutor<ActivityHistory> {

    Page<ActivityHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    List<ActivityHistory> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
