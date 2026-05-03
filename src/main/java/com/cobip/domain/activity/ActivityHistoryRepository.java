package com.cobip.domain.activity;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityHistoryRepository extends JpaRepository<ActivityHistory, Long> {

    Page<ActivityHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
