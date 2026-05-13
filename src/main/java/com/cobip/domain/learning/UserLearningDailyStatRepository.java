package com.cobip.domain.learning;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLearningDailyStatRepository extends JpaRepository<UserLearningDailyStat, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO user_learning_daily_stats (user_id, activity_date, study_seconds, created_at, updated_at)
        VALUES (:userId, :activityDate, :studySeconds, now(), now())
        ON CONFLICT (user_id, activity_date)
        DO UPDATE SET
            study_seconds = user_learning_daily_stats.study_seconds + EXCLUDED.study_seconds,
            updated_at = now()
        """, nativeQuery = true)
    int addStudySeconds(
        @Param("userId") Long userId,
        @Param("activityDate") LocalDate activityDate,
        @Param("studySeconds") long studySeconds
    );

    Optional<UserLearningDailyStat> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    List<UserLearningDailyStat> findByUserIdAndActivityDateBetween(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );

    @Query("select coalesce(sum(stat.studySeconds), 0) from UserLearningDailyStat stat where stat.user.id = :userId")
    long sumStudySecondsByUserId(@Param("userId") Long userId);
}
