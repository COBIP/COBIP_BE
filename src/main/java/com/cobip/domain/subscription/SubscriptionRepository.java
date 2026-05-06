package com.cobip.domain.subscription;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long userId);

    @Query("""
            select count(s)
            from Subscription s
            where s.status = com.cobip.domain.subscription.SubscriptionStatus.ACTIVE
              and (s.expiredAt is null or s.expiredAt >= :today)
            """)
    long countActiveSubscriptions(@Param("today") LocalDate today);
}
