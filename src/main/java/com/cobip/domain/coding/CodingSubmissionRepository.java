package com.cobip.domain.coding;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {

    @Query("""
            select distinct submission.problem.id
            from CodingSubmission submission
            where submission.user.id = :userId
              and submission.problem.id in :problemIds
              and submission.status = :status
            """)
    Set<Long> findSolvedProblemIdsByUserIdAndProblemIds(
        @Param("userId") Long userId,
        @Param("problemIds") Collection<Long> problemIds,
        @Param("status") CodingSubmissionStatus status
    );
}
