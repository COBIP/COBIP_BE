package com.cobip.domain.subscription;

import java.util.Optional;

import com.cobip.domain.user.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(User user) {
        if (user == null) {
            return false;
        }
        return subscriptionRepository.findByUserId(user.getId())
                .map(Subscription::isActive)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findByUser(User user) {
        return subscriptionRepository.findByUserId(user.getId());
    }
}
